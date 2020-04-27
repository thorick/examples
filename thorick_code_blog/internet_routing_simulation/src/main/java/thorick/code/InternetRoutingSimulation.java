package thorick.code;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Demo simulates a network in which new members or connections can be added at any time
 * <p>
 * The primary point of the demo is to show that as new nodes are added, the most cost-efficient
 * way to route a message between nodes is updated.
 * The harder problem of dealing with nodes that drop out is ignored and out of scope of this simple demo
 * whose primary purpose is to show the use of a distributed Bellman-Ford graph single source shortest paths algorithm.
 * <p>
 * Simulating the decentralized nature of the internet the routing information and calculation is
 * distributed to the nodes.
 * <p>
 * Minimum cost routing from node to node is computed using distributed Bellman-Ford for each network node.
 * This is a high cost way of computing All Pairs Shortest Paths but the benefit is that it is decentralized.
 * <p>
 * Each node controls the distributed calculation of the minimum paths to itself.
 * Because of the way that Bellman-Ford Dynamic Programming approach works, the progress of the calculation
 * depends on the computation proceeding in being discrete steps.
 * <p>
 * This is synchronous distributed algorithm progress.
 * One step must complete before the next step begins.
 * <p>
 * In the general case a network might elect a leader to act as the administrator that coordinates the beginning
 * and ending of a discrete step.  A consensus algorithm might be used to guarantee that every node agrees upon
 * which node is the administrator so that there is only one.
 * <p>
 * In our case we can dispense with the overhead and complexity of a consensus algorithm by taking advantage
 * of the parallel but sequential nature of the algorithm.
 * <p>
 * We process nodes in synchronous batches in which the next batch of nodes to be processed are those that are
 * reachable from the nodes of the current batch.
 * The actual algorithm involves the edges between the nodes but we use the nodes as the primary objects
 * that the implementation controls.
 * <p>
 * A synchronous batch is complete after the last unprocessed node has been used in a calculation.
 * For each batch there is a 'Leader Node'.
 * The Leader Node knows what tne next set of nodes are from which the next set of edges will come from.
 * The batch calculation is done concurrently with each node tracking when it has finished computing
 * all of its new edges.  After a node completes it reports back to the Leader Node of its completion status.
 * When the Leader Node receives the completion notice from the last remaining non-complete node
 * then it knows that the current batch is complete and that is it time to kick off the next batch.
 * There is a next batch if there are still more nodes that have not been reached by previous batches.
 * <p>
 * When it is time for the next batch to begin, the first node arbitrarily plucked from the Set next nodes becomes
 * the designated next Leader Node.
 * The Leader Node ID once chosen is propagated to forward to all participants in the cascade of network
 * messages that coordinate the next batch of calculations.
 * <p>
 * <p>
 * <p>
 * Simplifications:
 * - We're not using TCP.  To simulate a network with network communications, the network nodes each run
 * in their own independent threads and communicate via messages passed between each nodes queues.
 * No messages are ever lost.
 * <p>
 * - We assume that no nodes or connections drop out of the network.  We only add new nodes or connections.
 * No message ever has to route around a lost node.
 * <p>
 * - Because no nodes ever drop out, there is no backup state maintenance as there normally would be
 * in case the Leader Node gets lost and has to be reconstituted at a different node.
 * <p>
 * - When a new connection is added the entire network is recomputed.  We only really have to recompute
 * the connections that are affected by the single new connection.  That's an interesting problem but
 * not addressed here.
 * <p>
 * <p>
 * This code is intentionally written in a single monolithic class so that it can be easily
 * copied and compiled as it is just a single java source file.  Normally, you might not do this.
 */
public class InternetRoutingSimulation {
    // print detailed debug info
    static boolean DEBUG = false;

    // print higher level algorithm progress
    static boolean DEBUG_PROTOCOL = true;

    // only compute the paths to a single node
    static boolean DEBUG_DO_ONLY_ONE_SOURCE_NODE_CALCULATION = false;

    // only show higher level algorithm progress for a single source node
    static boolean DEBUG_FILTER_OUTPUT_ONE_SINGLE_SOURCE_NODE = true;
    static int DEBUG_FILTER_OUTPUT_ONE_SINGLE_SOURCE_NODE_NUMBER = 1;   // specify which single source to show
    static boolean PRINT_FINAL_ROUTING_TABLE = true;    // print the min cost path routing table after each network computation


    static int LOWEST_NODE_NUMBER = 1;
    static AtomicInteger NEXT_NODE_NUMBER = new AtomicInteger(LOWEST_NODE_NUMBER);
    public static AtomicBoolean GLOBAL_ROUTE_MAP_LOCKED = new AtomicBoolean(false);
    static AtomicInteger GLOBAL_ROUTE_MAP_LOCK_COUNT = new AtomicInteger();
    public static int GLOBAL_ROUTE_POST_COMPUTE_SLEEPTIME = 2000;
    static Edge CURRENT_CONNECTION_EDGE = null;

    public enum MessageType {
        GLOBAL_ROUTE_MAP_LOCK("GLOBAL_ROUTE_MAP_LOCK"),
        NEW_CONNECTION("NEW_CONNECTION"),
        BEGIN_SOURCE_ROUND_CALCULATION("BEGIN_SOURCE_ROUND_CALCULATION"),
        SOURCE_ROUND_LEADER_BEGIN_CALCULATION("SOURCE_ROUND_LEADER_BEGIN_CALCULATION"),
        SINGLE_NODE_CALCULATION("SINGLE_NODE_CALCULATION"),
        SINGLE_NODE_CALCULATIONS_COMPLETE("SINGLE_NODE_CALCULATION_COMPLETE"),
        NODE_EDGE_CALCULATION("NODE_EDGE_CALCULATION"),
        NODE_EDGE_CALCULATION_COMPLETE("NODE_EDGE_CALCULATION_COMPLETE"),
        SEND_MESSAGE("SEND_MESSAGE");
        String name;

        MessageType(String s) {
            this.name = s;
        }
    }

    static String line = "-------------------------------------------------------";

    // In real life there would be no global map
    // This would be distributed information
    // But this is to simply the demo
    Map<Integer, NetNode> nodeMap = new HashMap<>();
    private final int NO_VALUE = Integer.MAX_VALUE;
    private GraphAdjList g = new GraphAdjList();
    static int NUM_THREADS = 30;
    private volatile BlockingQueue<Runnable> poolQueue = new LinkedBlockingQueue<Runnable>();
    private volatile ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(NUM_THREADS,  // core pool size
                    NUM_THREADS * 2, // max pool size
                    1000000L,  // keepAlive msec
                    TimeUnit.MILLISECONDS,   // Time Units
                    poolQueue,      // pool
                    threadFactory);

    static void WAIT_GRAPH_OPEN() {
        while (GLOBAL_ROUTE_MAP_LOCKED.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        p("go to sleep " + GLOBAL_ROUTE_POST_COMPUTE_SLEEPTIME + " to allow network changes to propagate\n");
        try {
            Thread.sleep(GLOBAL_ROUTE_POST_COMPUTE_SLEEPTIME);    // give the new connection time to settle into the network
        } catch (InterruptedException e) {
        }
    }

    NetNode createNewNetNode() {
        Integer nodeNumber = NEXT_NODE_NUMBER.getAndIncrement();
        LinkedNode linkedNode = g.getAdjList(nodeNumber);
        NetNode node = new NetNode(nodeNumber);
        nodeMap.put(nodeNumber, node);
        threadPoolExecutor.execute(node);
        p("NEW NODE " + nodeNumber + " CREATED and RUNNING.");
        return node;
    }

    // this is the analogue of registering your node with a network and getting an address
    //
    boolean newConnection(int source, int dest, int cost) {
        NetNode sNode = nodeMap.get(source);
        NetNode dNode = nodeMap.get(dest);
        if (sNode == null || dNode == null) {
            throw new RuntimeException("Attempt to connect 2 nodes at least one of which does not exist: (" + source + "<-->" + dest + ")");
        }
        Edge e = g.getEdge(source, dest);
        if (e != null) {
            if (e.getValue() == cost) {
                return true;    // this connection is already done with this cost
            }
        }
        // normally we'd do this distributed, but simplified and just do it here for the demo
        // since this isn't the main point.
        boolean weLockedGlobalRouteMap = GLOBAL_ROUTE_MAP_LOCKED.compareAndSet(false, true);
        if (!weLockedGlobalRouteMap) {
            cons("\n\n------------------------------------------------");
            cons("COULD NOT LOCK  GLOBAL_ROUTE_MAP_LOCKED  LOCK.  new connection [" + source + "-" + dest + "] REFUSED !");
            cons("------------------------------------------------\n\n");
            return false;       // fail..
        }

        // create new edge bidirectional
        Edge e1 = g.newEdge(source, dest, cost);
        g.insert(e1);
        sNode.edges.put(dest, e1);
        sNode.initializeForGlobalCalculation();
        NetNode.TableEntry te1 = sNode.new TableEntry(source, dest, cost, dest);
        sNode.nodeTable.put(dest, te1);

        CURRENT_CONNECTION_EDGE = g.newEdge(source, dest, cost);

        Edge e2 = g.newEdge(dest, source, cost);
        g.insert(e2);
        dNode.edges.put(source, e2);
        dNode.initializeForGlobalCalculation();
        NetNode.TableEntry te2 = dNode.new TableEntry(dest, source, cost, source);
        dNode.nodeTable.put(source, te2);

        // find the leader node and kick off route computation
        NetNode.SourceRoundMessage m = sNode.new SourceRoundMessage(MessageType.NEW_CONNECTION, 0, source, dest, 0);
        m.cost = cost;
        networkSend(source, m);
        return true;
    }

    //
    //  simulates the transmission and reception of a packet
    //  over the network to a destination node
    //
    public void networkSend(int destination, NetNode.SourceRoundMessage message) {
        NetNode node = nodeMap.get(destination);
        message.addNodePath(destination);
        node.putMessage(message);
    }

    private static boolean isP() {
        return DEBUG;
    }

    private static void p(String s) {
        if (isP()) {
            System.err.println(s);
        }
    }

    private static void proto(NetNode node, NetNode.SourceRoundMessage m, String s) {
        if (DEBUG_PROTOCOL) {
            String singleSourceString = "";
            if (m instanceof NetNode.SourceRoundMessage) {
                NetNode.SourceRoundMessage srm = (NetNode.SourceRoundMessage) m;
                int calcSingleSourceNode = srm.calcSingleSourceNode;
                if (DEBUG_FILTER_OUTPUT_ONE_SINGLE_SOURCE_NODE) {
                    if (calcSingleSourceNode != DEBUG_FILTER_OUTPUT_ONE_SINGLE_SOURCE_NODE_NUMBER) {
                        return;     //  don't print this message
                    }
                }
                singleSourceString = "SSN(" + calcSingleSourceNode + ")";
            }
            System.err.println(CURRENT_CONNECTION_EDGE + "-" + singleSourceString + "(" + node.getName() + "PROTO) " + m.messageType + " " + node.getSym() + "[" + m.from + "->" + m.to + "]: " + s);
        }
    }

    private static String sourceRoundLeaderString(int i) {
        return " sourceRoundLeader(" + i + ")";
    }

    private static String sourceRoundLeaderObject(NetNode.PathCalcElement pce, int ssLeader) {
        return " SSN[" + pce.singleSourceNode + "].node(" + pce.nodeId + ")." + "LEADER[" + ssLeader + "].{" + pce.hashCode() + "}";
    }

    private static void cons(String s) {
        System.err.println(s);
    }


    //
    //  There is one instance of this class for each Node in the network
    //  The NetNode message queue simulates the network connection
    //  The messages on the queue simulate network messages
    //
    public class NetNode implements Runnable {
        final int netNodeNumber;
        boolean done = false;
        BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();

        // shortest hop from our node to the final destination anywhere on the network
        Map<Integer, TableEntry> nodeTable = new HashMap<>();

        // for each source node route calculation there is a set of iteration data for this node
        Map<Integer, PathCalcElement> pathCalcElementTable = new HashMap<>();
        Map<Integer, Edge> edges = new HashMap<>();             // the edges to our immediate neighbors
        Set<Integer> childCalculations = new HashSet<>();

        public NetNode(int number) {
            this.netNodeNumber = number;
        }

        public boolean isLeader() {
            // fixed for this simplified demo where there is no node failure, else we'd compute
            return netNodeNumber == getLeaderId();
        }

        public int getLeaderId() {
            return LOWEST_NODE_NUMBER;
        }

        public int getSecondaryId() {
            // fixed for this simplified demo where there is no node failure
            return LOWEST_NODE_NUMBER + 1;
        }

        public void addChildCalculation(int i) {
            childCalculations.add(i);
        }

        public void removeChildCalculation(int i) {
            childCalculations.remove(i);
        }

        public boolean allCalculationsComplete() {
            return childCalculations.size() <= 0;
        }

        public boolean initializeForGlobalCalculation() {
            PathCalcElement e;
            for (int i = LOWEST_NODE_NUMBER; i < NEXT_NODE_NUMBER.get(); i++) {
                e = pathCalcElementTable.get(i);
                if (e == null) {
                    e = new PathCalcElement(netNodeNumber, i);
                    pathCalcElementTable.put(i, e);
                }
                e.beforeGlobalCalculationReset();
            }
            nodeTable.clear();      // recompute the entire table for the new graph
            return true;
        }

        public int getNetNodeNumber() {
            return netNodeNumber;
        }

        public String getName() {
            return "Node(" + netNodeNumber + ") ";
        }

        public String getSym() {
            return "(" + netNodeNumber + ")";
        }

        //
        // The original send of a message from this node
        //
        public void send(SourceRoundMessage message) {
            int to = message.to;
            TableEntry entry = nodeTable.get(to);
            if (entry == null) {
                throw new RuntimeException(getName() + " attempt to send to unknown destination: (" + to + ")");
            }
            message.addNodePath(netNodeNumber);
            int firstHop = entry.firstHopNode;
            Edge e = g.getEdge(netNodeNumber, firstHop);
            if (e == null) {
                throw new RuntimeException(getName() + " attempt to get an unknown graph edge: [" + netNodeNumber + "," + firstHop + "]");
            }
            int hopCost = e.getValue();
            message.addPathCost(hopCost);
            networkSend(firstHop, message);
        }

        public boolean processReceivedMessage(SourceRoundMessage message) {
            //cons(getName()+" processReceivedMessage  from ("+message.from+") '"+message.message+"'");
            boolean success = false;
            StringBuilder sb = new StringBuilder(line + "\n" + getName());
            sb.append("received message: " + message.message + "\n");
            sb.append("   path: " + message.printNodePath() + "\n");
            sb.append("   cost: " + message.printPathCost() + "\n");
            sb.append(line + "\n");
            cons(sb.toString());
            success = true;
            return success;
        }

        //  from this node add a new connection
        //  if this is a connection to an existing node then the number of that node is an arg
        //  if this is a new node then the number of that node is NULL and a new node is created
        //
        //  first we must check to see if it's safe to add now
        //  in our simplified demo, we can only add when there is no route remapping in progress
        public boolean addNewConnectionTo(Integer destNetNodeNumber, int cost) {
            if (destNetNodeNumber == null) {
                destNetNodeNumber = NEXT_NODE_NUMBER.getAndIncrement();
            }
            Edge e = edges.get(destNetNodeNumber);
            if (e != null) {
                if (e.getValue() == cost) {
                    return false;    // we already have this connection with this cost
                }
            }
            e = new Edge(netNodeNumber, destNetNodeNumber, cost);
            edges.put(destNetNodeNumber, e);

            // send NEW_CONNECTION message to new node
            // there is currently no registered network link between us anywhere
            SourceRoundMessage message = new SourceRoundMessage(MessageType.NEW_CONNECTION, 0, netNodeNumber, destNetNodeNumber, 0);
            message.cost = cost;
            TableEntry newTableEntry = new TableEntry(netNodeNumber, destNetNodeNumber, cost, destNetNodeNumber);
            nodeTable.put(destNetNodeNumber, newTableEntry);
            e = new Edge(netNodeNumber, destNetNodeNumber, cost);

            CURRENT_CONNECTION_EDGE = new Edge(netNodeNumber, destNetNodeNumber, cost);

            g.insert(e);
            Edge e2 = new Edge(destNetNodeNumber, netNodeNumber, cost);
            g.insert(e2);
            networkSend(destNetNodeNumber, message);
            return true;
        }

        public void putMessage(Message message) {
            try {
                //p(getName()+" +++++   enter put message on queue "+message.messageType+"  '"+message.message);
                messageQueue.put(message);
                //p(getName()+" +++++   return from put message on queue "+message.messageType+"  '"+message.message+"' messageQueue.size(): "+messageQueue.size());
            } catch (InterruptedException e) {
            }
        }

        boolean lockRouteMap() {
            boolean weLockedOK = false;
            if (isLeader()) {
                weLockedOK = GLOBAL_ROUTE_MAP_LOCKED.compareAndSet(false, true);
                if (weLockedOK) {
                    // todo:  set lock timeout to cancel any hanging job
                }
                return weLockedOK;
            }
            int leaderId = getLeaderId();
            return true;
        }

        public void run() {
            SourceRoundMessage message = null;
            while (!done) {
                //p(getName()+" top of run()  messageQueue.size(): "+messageQueue.size());
                try {
                    message = (SourceRoundMessage) messageQueue.take();
                    //p(getName()+" +++++   return from take message from queue "+message.messageType+"  '"+message.toString()+"'");
                } catch (InterruptedException e) {
                }
                int to = message.to;
                // we're not the destination node, so relay along the network
                if (to != netNodeNumber) {
                    proto(this, message, " **ROUTING**  message bound for (" + to + ") need to route it.");
                    TableEntry tableEntry = nodeTable.get(to);
                    if (tableEntry == null) {
                        throw new RuntimeException(getName() + " Could not find tableEntry for destination node (" + to + ") for message: " +
                                message.messageType + "-" + message.message);
                    }
                    int firstHop = tableEntry.firstHopNode;
                    proto(this, message, " **ROUTING**   message bound for (" + to + ")  route to firstHop (" + firstHop + ")  message: '" + message.message + "'");
                    Edge e = g.getEdge(netNodeNumber, firstHop);
                    if (e == null) {
                        throw new RuntimeException(getName() + " attempt to get an unknown graph edge: [" + netNodeNumber + "," + firstHop + "]");
                    }
                    int hopCost = e.getValue();
                    message.addPathCost(hopCost);
                    networkSend(firstHop, message);
                    continue;
                }
                int from = message.from;

                //p(getName()+" +++++      message from queue "+message.messageType+"  from: '"+from+"'");
                switch (message.messageType) {
                    // we have received a NEW_CONNECTION request
                    // It comes from a direct connection from a node for whom our connection to it is not known to the network yet
                    // set it up
                    case NEW_CONNECTION:
                        proto(this, message, "BEGIN ");
                        if (DEBUG_DO_ONLY_ONE_SOURCE_NODE_CALCULATION) {
                            GLOBAL_ROUTE_MAP_LOCK_COUNT.set(1);
                        } else {
                            GLOBAL_ROUTE_MAP_LOCK_COUNT.set(g.getHighestVertexNumber());
                        }
                        //cons("\n---------------------  GLOBAL_ROUTE_MAP_LOCK_COUNT initialized to " + GLOBAL_ROUTE_MAP_LOCK_COUNT.intValue()+
                        //        "  --------------------- \n");
                        for (int i = LOWEST_NODE_NUMBER; i < NEXT_NODE_NUMBER.get(); i++) {
                            NetNode n = nodeMap.get(i);
                            n.initializeForGlobalCalculation();
                        }
                        for (int i = LOWEST_NODE_NUMBER; i < NEXT_NODE_NUMBER.get(); i++) {
                            NetNode n = nodeMap.get(i);
                            SourceRoundMessage SRMessage =
                                    new SourceRoundMessage(MessageType.BEGIN_SOURCE_ROUND_CALCULATION,
                                            i,
                                            i,
                                            i,
                                            i);
                            proto(this, message, "SEND " + SRMessage.messageType + " for node (" + i + ")  message: " + SRMessage);
                            networkSend(i, SRMessage);
                            if (DEBUG_DO_ONLY_ONE_SOURCE_NODE_CALCULATION) {
                                proto(this, message, "DO ONLY ONE SINGLE SOURCE CALC from: " + n.getName());
                                i = NEXT_NODE_NUMBER.get();
                            }
                        }
                        proto(this, message, "END.");
                        break;

                    //
                    //  This runs on the Round Source Node
                    //
                    //  OK, this will be the initial kick off of the first execution
                    //  of the children of a single source node calculation
                    //  setup the single source node as the round leader for this first round
                    //  then send kick off the executions
                    //  from then on out, the distributed algorithm propagates itself until completion
                    //  for this single source computation
                    //
                    //
                    case BEGIN_SOURCE_ROUND_CALCULATION:
                        // todo:  wrong message
                        SourceRoundMessage SRNMessage = (SourceRoundMessage) message;
                        int singleSourceNode = SRNMessage.getCalcSingleSourceNode();
                        Integer sourceRoundLeader = SRNMessage.roundLeaderNode;
                        // our element for the calculation being run by the source node for the shortest paths to itself
                        PathCalcElement roundLeaderPCElement = pathCalcElementTable.get(singleSourceNode);
                        proto(this, message, "+++++++++++  START initial round using " + sourceRoundLeaderObject(roundLeaderPCElement, sourceRoundLeader) + "  the message is: " + SRNMessage.toString() +
                                roundLeaderPCElement.getChildNodeContents());
                        SourceRoundMessage srm = new SourceRoundMessage(MessageType.SINGLE_NODE_CALCULATION,
                                singleSourceNode,     // the single source node
                                netNodeNumber,
                                netNodeNumber,
                                sourceRoundLeader);                // the leader of the next round
                        Stack<SourceRoundMessage> stack = roundLeaderPCElement.getNodeLeaderSingleNodeCalculationStack();
                        stack.add(srm);
                        proto(this, message, " sending message: " + srm.toString());
                        networkSend(netNodeNumber, srm);
                        break;

                    // we get here after a previous round leader has completed
                    // we are the new round leader
                    // we must set ourselves to take over as the new round leader
                    // then kick off the first node calculation
                    //
                    // our stack of SOURCE_ROUND_MESSAGES to run have been loaded up
                    // and this is our signal to begin processing the nodes 'single file'
                    case SOURCE_ROUND_LEADER_BEGIN_CALCULATION:
                        proto(this, message, "NODE(" + netNodeNumber + ") BEGIN ");
                        SourceRoundLeaderBeginCalculationMessage srlbcMessage =
                                (SourceRoundLeaderBeginCalculationMessage) message;
                        singleSourceNode = message.getCalcSingleSourceNode();
                        PathCalcElement pcElement = pathCalcElementTable.get(singleSourceNode);

                        // setup our state to handle taking on the role of round leader
                        pcElement.roundLeaderReset();
                        PathCalcElement.RoundLeaderStateHolder previousRoundLeaderState =
                                srlbcMessage.getPreviousRoundLeaderState();
                        previousRoundLeaderState.transferToNew(pcElement);
                        proto(this, message,
                                sourceRoundLeaderObject(pcElement, netNodeNumber) +
                                        "  After transfer of previous round leader state, our child node contents are: "+
                                        pcElement.getChildNodeContents());

                        //Stack<SourceRoundMessage> srmStack = pcElement.getNodeLeaderSingleNodeCalculationStack();
                        int nextRoundLeaderProcessedChildCount = 0;
                        for (Integer i : pcElement.childNodes) {
                            // queue up all unprocessed nodes
                            if (!pcElement.allCompletedChildNodes.contains(i)) {
                                SourceRoundMessage srm1 =
                                        new SourceRoundMessage(MessageType.SINGLE_NODE_CALCULATION,
                                                singleSourceNode,     // the single source node
                                                this.netNodeNumber,
                                                i,
                                                this.netNodeNumber);                // the leader of the next round
                                pcElement.addNodeLeaderSingleNodeCalculationMessage(srm1);
                                nextRoundLeaderProcessedChildCount++;
                                proto(this, message, "NODE(" + netNodeNumber +
                                        ") queued up SINGLE_NODE_CALCULATION for node(" + i +
                                        ").");
                            } else {
                                proto(this, message, "NODE(" + netNodeNumber +
                                        ") skip SINGLE_NODE_CALCULATION on next node(" + i +
                                        ") because it has already been completed.");
                            }
                        }
                        if (nextRoundLeaderProcessedChildCount <= 0) {
                            // there are no more nodes to process
                            cons("FATAL ERROR !  there are NO next nodes to process but we are in SOURCE_ROUND_LEADER_BEGIN_CALCULATION !");
                            break;
                        }
                        SourceRoundMessage nextSourceRoundMessage =
                                pcElement.nextNodeLeaderSingleNodeCalculationMessage();
                        if (nextSourceRoundMessage != null) {
                            int singleNodeToCalculate = nextSourceRoundMessage.to;
                            networkSend(singleNodeToCalculate, nextSourceRoundMessage);
                        } else {
                            cons("FATAL ERROR in SOURCE_ROUND_LEADER_BEGIN_CALCULATION: "+
                                 "SOURCE_ROUND_MESSAGE stack empty when it should NOT be ! \n"+
                                    "Less optimal routing may result.");
                        }
                        break;
                    // this runs on the node which is performing the node calculation
                    //
                    // this is run by each node of a single round
                    //  this node sends out out individual NODE_EDGE_CALCULATIONS to the nodes on its edges
                    //
                    //
                    case SINGLE_NODE_CALCULATION:
                        SourceRoundMessage SRMessage = (SourceRoundMessage) message;
                        sourceRoundLeader = SRMessage.roundLeaderNode;
                        proto(this, message, "BEGIN(" + netNodeNumber + ")   the message is: " + SRMessage.toString());
                        singleSourceNode = SRMessage.getCalcSingleSourceNode();
                        PathCalcElement PCElement = pathCalcElementTable.get(singleSourceNode);
                        PCElement.regularNodePerRoundReset();     // initialize our node for edge calculations
                        // find our children, setup for them and go
                        LinkedNode n = g.getAdjList(netNodeNumber);
                        while (n != null) {
                            Edge e1 = n.edge();
                            proto(this, message, "NODE(" + netNodeNumber + ") " + e1 + "  check edge to see if it is live.");
                            // if this edge is as yet unprocessed
                            if (PCElement.isEdgeLive(e1)) {
                                int currCost = PCElement.currDistTo;
                                int toNode = e1.dest;
                                PCElement.addEdgeNode(toNode);   // register the edge dest node
                                NodeEdgeCalculationMessage SNCMessage =
                                        new NodeEdgeCalculationMessage(singleSourceNode,
                                                netNodeNumber,
                                                toNode,
                                                SRMessage.getRoundLeaderNode(),
                                                currCost,
                                                e1);
                                proto(this, message, "NODE(" + netNodeNumber + ") " + e1 + " SEND to EdgeCalc(" + toNode + ") the message is: " + SNCMessage.toString());
                                networkSend(toNode, SNCMessage);
                            } else {
                                proto(this, message, "NODE(" + netNodeNumber + ") " + e1 + "  SKIP edge is NOT live.");
                            }
                            n = n.next();
                        }
                        break;

                    // this is a single distributed calculation within a round at a node, us.
                    // it is performed concurrently by a thread from the thread pool
                    // there may be many simultaneous calculations being done on this node
                    // by concurrent calculations by different source nodes
                    //
                    case NODE_EDGE_CALCULATION:
                        //proto(this, message, "BEGIN ");
                        NodeEdgeCalculationMessage SRNCMessage = (NodeEdgeCalculationMessage) message;
                        NodeEdgeCalculationTask edgeCalcTask =
                                new NodeEdgeCalculationTask(this, SRNCMessage);
                        threadPoolExecutor.execute(edgeCalcTask);
                        break;

                    // this runs on the node that has sent out INDIVIDUAL_NODE_CALCULATIONS to it's children
                    //  An individual child calculation sends back this message after it finishes
                    //
                    // account for the completion of the INDIVIDUAL_NODE_CALCULATION
                    //  when we have received replies from all of our children
                    //  then we notify the ROUND LEADER with
                    //    SINGLE_NODE_CALCULATION_COMPLETE
                    case NODE_EDGE_CALCULATION_COMPLETE:
                        //proto(this, message, "BEGIN ");
                        NodeEdgeCalculationCompleteMessage NECCompleteMessage =
                                (NodeEdgeCalculationCompleteMessage) message;
                        PCElement = pathCalcElementTable.get(NECCompleteMessage.calcSingleSourceNode);
                        // add this edge to our seen set so that we don't process it twice
                        PCElement.addSeenEdge(NECCompleteMessage.edgeFrom);
                        PCElement.edgeNodeNewEdgeCount += NECCompleteMessage.newEdgeCount;
                        PCElement.addAllNextRoundChildNodes(NECCompleteMessage.nextRoundChildNodes);
                        boolean isNodeComplete = PCElement.addCompletedEdgeNode(message.from);
                        if (isNodeComplete) {
                            //proto(this, message, "NODE("+netNodeNumber+") PROCESSING IS COMPLETE, run EDGE CALC COMPLETE TASK");
                            sourceRoundLeader = NECCompleteMessage.roundLeaderNode;
                            SingleNodeCalculationsCompleteMessage SNCCompleteMessage =
                                    new SingleNodeCalculationsCompleteMessage(NECCompleteMessage.calcSingleSourceNode,
                                            netNodeNumber,
                                            sourceRoundLeader,
                                            sourceRoundLeader,
                                            PCElement.nextRoundChildNodes,
                                            PCElement.edgeNodeNewEdgeCount);
                            proto(this, NECCompleteMessage, "NODE(" + this.netNodeNumber + ") is COMPLETE send " + SNCCompleteMessage.messageType + " message to " + sourceRoundLeaderString(sourceRoundLeader));
                            networkSend(sourceRoundLeader, SNCCompleteMessage);
                        }
                        proto(this, message, "NODE(" + netNodeNumber + ") SINGLE EDGE CALCULATION DONE.");
                        break;

                    // this is handled by the source round leader node to synchronize
                    // the end of a round and the start of the next round
                    //
                    // it is invoked when a node has finished calculating all of its outgoing edge calculations
                    // and is done.
                    //
                    // the whole round is done if all of the nodes run and tracked by this source round leader
                    // have reported back as completed.
                    //
                    //
                    case SINGLE_NODE_CALCULATIONS_COMPLETE:
                        //p(getName()+" +++++   top of case SINGLE_NODE_CALCULATIONS_COMPLETE: "+message.messageType+"  '"+message.toString()+"'");
                        proto(this, message, "NODE(" + netNodeNumber + ") BEGIN ");
                        singleSourceNode = message.getCalcSingleSourceNode();
                        sourceRoundLeader = message.roundLeaderNode;    // this better be US !
                        //p(getName()+" +++++   top of case SINGLE_NODE_CALCULATIONS_COMPLETE: after proto() call");
                        SingleNodeCalculationsCompleteMessage SRCompleteMessage = (SingleNodeCalculationsCompleteMessage) message;

                        // we need the element that corresponds to the single source calculation that is using us as the SOURCE ROUND LEADER NODE
                        PathCalcElement SourceRoundLeaderNodePCElement = pathCalcElementTable.get(singleSourceNode);
                        proto(this, message, sourceRoundLeaderObject(SourceRoundLeaderNodePCElement, sourceRoundLeader) + " about to do addCompletedChildNode(" + from + ") on SourceRoundLeaderNodePCElement(" + SourceRoundLeaderNodePCElement.nodeId + "): " + SourceRoundLeaderNodePCElement.getChildNodeContents());
                        SourceRoundLeaderNodePCElement.addCompletedChildNode(from);
                        proto(this, message, sourceRoundLeaderObject(SourceRoundLeaderNodePCElement, sourceRoundLeader) + " after add of completedChildNode(" + from + ") " + SourceRoundLeaderNodePCElement.getChildNodeContents());
                        //p(getName()+" +++++  SINGLE_NODE_CALCULATIONS_COMPLETE: after add of completedChildNode("+from+") on "+sourceRoundLeaderObject(SourceRoundLeaderNodePCElement)+": "+SourceRoundLeaderNodePCElement.getChildNodeContents());

                        // update the single source computation complete edge count
                        SourceRoundLeaderNodePCElement.globalNewEdgeCount += SRCompleteMessage.newEdgeCount;

                        // add the accumulated nextRoundChildNodes from the sending completed node
                        SourceRoundLeaderNodePCElement.addAllNextRoundChildNodes(SRCompleteMessage.nextNodes);
                        //p(getName()+" +++++  SINGLE_NODE_CALCULATIONS_COMPLETE: after addAllNextRoundChildNodes() on "+sourceRoundLeaderObject(SourceRoundLeaderNodePCElement, sourceRoundLeader)+": "+SourceRoundLeaderNodePCElement.getChildNodeContents());

                        // We're the source round leader node, so we're tracking all of the calc nodes for this round
                        // if the completedChildCount == the childCount then this round is done
                        // else we're not done and we send off the next node in the queue
                        if (!SourceRoundLeaderNodePCElement.isRoundComplete()) {
                            //p(getName()+" +++++   isRoundCOmplete() == false   break !");
                            proto(this, message, "NODE(" + netNodeNumber + ")  isRoundComplete() == false  exit, start next Node Calculation edges.");

                            SourceRoundMessage nextSourceRoundMessage2 =
                                    SourceRoundLeaderNodePCElement.nextNodeLeaderSingleNodeCalculationMessage();
                            if (nextSourceRoundMessage2 != null) {
                                int singleNodeToCalculate = nextSourceRoundMessage2.to;
                                networkSend(singleNodeToCalculate, nextSourceRoundMessage2);
                                proto(this, message, sourceRoundLeaderObject(SourceRoundLeaderNodePCElement, sourceRoundLeader) +
                                        "+++++  Leader Node: we've just kicked off edge calcs for the next node ["+singleNodeToCalculate+"]");
                                break;       // kicked off next node calc, we're done here
                            }
                            cons("UNEXPECTED null nextSourceRoundMessage2 in SINGLE_NODE_CALCULATIONS_COMPLETE\n"+
                                    "  we should have a next node to process queued up to go, but there is NOT one !\n"+
                                    "  calculation will continue on anyway.");
                            // we should not have reached here, but if we did then we've completed the round
                            // so continue
                        }
                        // we're done with this round
                        // if there are still more edges to process
                        // then determine the next source round leader
                        // and kick off all of the next child source round calculations
                        // NOW we choose the next source round leader, it will be the first node in line
                        if (SourceRoundLeaderNodePCElement.nextRoundChildNodes != null) {
                            //p(getName()+" +++++   SourceRoundLeaderNodePCElement.nextRoundChildNodes != null  "+sourceRoundLeaderObject(SourceRoundLeaderNodePCElement, sourceRoundLeader));

                            Iterator<Integer> it = SourceRoundLeaderNodePCElement.nextRoundChildNodes.iterator();
                            if (it.hasNext()) {
                                Integer nextSourceRoundLeader = it.next();
                                //p(getName()+" +++++  nextSourceRoundLeader = "+nextSourceRoundLeader);
                                if (nextSourceRoundLeader != null) {
                                    int nextChildNodeToDoCount = 0;
                                    Set<Integer> nextRoundChildNodes = SourceRoundLeaderNodePCElement.nextRoundChildNodes;
                                    for (Integer i : nextRoundChildNodes) {
                                        if (!SourceRoundLeaderNodePCElement.allCompletedChildNodes.contains(i)) {
                                            nextChildNodeToDoCount++;
                                        }
                                    }
                                    // if there are more nodes to process then kick off the next round leader
                                    // else fall through and declare the computation for this single source node  COMPLETE
                                    if (nextChildNodeToDoCount > 0) {
                                        PathCalcElement.RoundLeaderStateHolder oldRoundLeaderState =
                                                SourceRoundLeaderNodePCElement.new RoundLeaderStateHolder(SourceRoundLeaderNodePCElement);
                                        SourceRoundLeaderBeginCalculationMessage beginCalculationMessage =
                                                new SourceRoundLeaderBeginCalculationMessage(
                                                        SRCompleteMessage.getCalcSingleSourceNode(),     // the single source node
                                                        this.netNodeNumber,
                                                        nextSourceRoundLeader,
                                                        nextSourceRoundLeader,
                                                        oldRoundLeaderState);
                                        networkSend(nextSourceRoundLeader, beginCalculationMessage);
                                        break;
                                    }
                                }
                            }
                        }
                        // this round is complete AND there are no more queued up nodes to run
                        // this means that the single source computation is complete for the graph
                        SourceRoundLeaderNodePCElement.singleSourceComputationComplete = true;
                        boolean metGlobalNewEdgeCountThreshold = false;
                        if (SourceRoundLeaderNodePCElement.globalNewEdgeCount >= g.getHighestVertexNumber()) {
                            metGlobalNewEdgeCountThreshold = true;
                        }
                        int lockedNodeCount = GLOBAL_ROUTE_MAP_LOCK_COUNT.decrementAndGet();
                        String message1 = "\n      NODE(" + netNodeNumber + ") After decrement for Single Source For One Node complete, Remaining GLOBAL_ROUTE_MAP_LOCK_COUNT=" + lockedNodeCount + ". " +
                                " (GLOBAL NEW EDGE COUNT=" + SourceRoundLeaderNodePCElement.globalNewEdgeCount +
                                ", HIGHEST GRAPH VERTEX NUMBER=" + g.getHighestVertexNumber() + "\n\n ";
                        proto(this, message,
                                sourceRoundLeaderString(netNodeNumber) + message1);
                        p("\n\n" + CURRENT_CONNECTION_EDGE + "-SSN(" + message.calcSingleSourceNode + ")" + getName() + " +++++   " + message1);
                        if (lockedNodeCount <= 0) {
                            proto(this, message,
                                    sourceRoundLeaderString(netNodeNumber) + " DID " + (metGlobalNewEdgeCountThreshold ? "" : "NOT ") +
                                            "meet ROUND COMPLETE edge threshold. " +
                                            "DECLARING SINGLE SOURCE NODE COMPUTATION COMPLETE for source (" + netNodeNumber + ")" +
                                            " UNLOCKING GLOBAL_ROUTE_MAP_LOCK");
                            GLOBAL_ROUTE_MAP_LOCKED.set(false);    // we've done all NODES  unlock !

                            if (PRINT_FINAL_ROUTING_TABLE) {
                                StringBuilder sb = new StringBuilder("Network Connection Computation complete.\n" +
                                        " Here are the results for the entire Network.\n");
                                for (int i = LOWEST_NODE_NUMBER; i < NEXT_NODE_NUMBER.intValue(); i++) {
                                    sb.append(" node(" + i + "):\n");
                                    Map<Integer, TableEntry> nodeTable = nodeMap.get(i).nodeTable;
                                    for (TableEntry te : nodeTable.values()) {
                                        sb.append("    " + te + "\n");
                                    }
                                }
                                boolean debugSave = DEBUG;
                                DEBUG = true;
                                p(sb.toString());
                                DEBUG = debugSave;
                            }
                        }
                        break;

                    case SEND_MESSAGE:
                        proto(this, message, "BEGIN  from (" + from + ").");
                        processReceivedMessage(message);
                        break;
                    default:
                        throw new RuntimeException(getName() + " UKNOWN messageType: " + message.messageType);
                }
            }
        }

        //
        // Runs on the destination node of the Edge from the Node doing the calculation
        //
        // for the current round for this source
        // perform the Bellman Ford computation
        // and determine the set of Edges to queue up for the next round
        // to be done on the next set of nodes
        // when we are done we report the status to the sourceRoundLeaderNode that controls this round
        //
        class NodeEdgeCalculationTask implements Runnable {
            final NodeEdgeCalculationMessage nodeMessage;
            final NetNode node;                  // the node doing the computing
            final int sourceRoundLeader;

            public NodeEdgeCalculationTask(NetNode n, NodeEdgeCalculationMessage m) {
                node = n;
                nodeMessage = m;
                sourceRoundLeader = m.roundLeaderNode;
            }

            @Override
            public void run() {
                // for the from node check the current cost of getting to this node
                // vs the tentative cost of using the tentative edge from the from node
                // decide and update
                PathCalcElement pathCalc = node.pathCalcElementTable.get(nodeMessage.calcSingleSourceNode);
                int fromNodeCost = nodeMessage.fromNodeCost;
                Edge e = nodeMessage.edgeFrom;

                int newEdgeCount = 0;
                Integer nextChildNode = null;
                // only handle this edge if we have not done so already
                // so if we do this, then we can trust ANY nextChildNode that we have added to the PathElement
                if (pathCalc.isEdgeLive(e)) {
                    proto(node, nodeMessage, "BEGIN(" + node.netNodeNumber + ") back edge: " + e);
                    int edgeCost = e.getValue();
                    int candidateCost = fromNodeCost + edgeCost;
                    if (fromNodeCost == NO_VALUE) {
                        candidateCost = edgeCost;    // if the other node has not yet been calculated for
                    }

                    if (candidateCost < pathCalc.currDistTo) {
                        proto(node, nodeMessage, "NODE(" + node.netNodeNumber + ") NEW LOWER VALUE for back edge: " + e + "  new lower value=" + candidateCost + " old higher value=" + pathCalc.currDistTo +
                                ", adding this node(" + e.dest + ") to nextChildNode");
                        // new lower value
                        pathCalc.currDistTo = candidateCost;
                        pathCalc.edgeTo = e;

                        // update the node with this latest lowest cost edge to the single source node
                        // during the calculation edges always 'go away' from the single source node
                        // for network routing the single source node is the destination of routing
                        // so the destination is is the source of the edge
                        TableEntry te = new TableEntry(e.dest,     // this node that owns this TableEntry
                                nodeMessage.calcSingleSourceNode,  // this is the network destination, the single source node
                                pathCalc.currDistTo,               // this is the cost of transmission to the first hop node (this edge cost)
                                e.source                           // the first hop node is the vertex of the edge at the single source node side
                        );
                        node.nodeTable.put(nodeMessage.calcSingleSourceNode, te);
                        nextChildNode = new Integer(e.dest);
                        newEdgeCount++;      // new edge connecting 2 nodes  increases the calc edge count
                    }
                } else {
                    proto(node, nodeMessage, "NODE(" + node.netNodeNumber + ")  SKIP " + e + " THIS EDGE POINTS TO COMPLETED NODE(" + e.dest + ")");
                }
                Set<Integer> nextRoundChildNodes = new HashSet<>();
                if (nextChildNode != null) {
                    nextRoundChildNodes.add(nextChildNode);
                }
                NodeEdgeCalculationCompleteMessage NECCompleteMessage =
                        new NodeEdgeCalculationCompleteMessage(nodeMessage.calcSingleSourceNode,
                                node.netNodeNumber,
                                nodeMessage.from,
                                nodeMessage.roundLeaderNode,
                                e,
                                nextRoundChildNodes,
                                newEdgeCount);
                networkSend(nodeMessage.from, NECCompleteMessage);
            }
        }

        //
        //  Each node holds one of these for every other source node in the network
        //  It holds the calculation state for the minimum paths originating from that source node
        //
        class PathCalcElement {
            final int nodeId;
            final int singleSourceNode;
            int sourceRoundLeader;      // this is the leader of this calculation round for this source
            int globalNewEdgeCount;     // this is the propagated complete new edge count
            // this is very important and determines when the calculation can stop
            // we depend on relaying the accumulated count to next sourceRoundLeaders
            Stack<SourceRoundMessage> nodeLeaderSingleNodeCalculationStack;
            boolean singleSourceComputationComplete;
            boolean roundComplete = false;      // a local node round is complete
            Set<Integer> childNodes;
            Set<Integer> completedChildNodes;
            Set<Integer> nextRoundChildNodes;
            Set<Integer> allCompletedChildNodes;
            Integer currDistTo = NO_VALUE;
            Edge edgeTo;
            boolean nodeComplete = false;
            Set<Integer> edgeNodes;
            Set<Integer> completedEdgeNodes;
            int edgeNodeNewEdgeCount;
            Set<Edge> seenEdges;

            public PathCalcElement(int id, int rl) {
                nodeId = id;
                singleSourceNode = rl;
                edgeNodes = new HashSet<>();
                completedEdgeNodes = new HashSet<>();
                seenEdges = new HashSet<Edge>();
                allCompletedChildNodes = new HashSet<>();
                regularNodePerRoundReset();
            }

            Stack<SourceRoundMessage> getNodeLeaderSingleNodeCalculationStack() {
                if (nodeLeaderSingleNodeCalculationStack == null) {
                    nodeLeaderSingleNodeCalculationStack = new Stack<SourceRoundMessage>();
                }
                return nodeLeaderSingleNodeCalculationStack;
            }
            void addNodeLeaderSingleNodeCalculationMessage(SourceRoundMessage m) {
                getNodeLeaderSingleNodeCalculationStack().push(m);
            }
            SourceRoundMessage nextNodeLeaderSingleNodeCalculationMessage() {
                return getNodeLeaderSingleNodeCalculationStack().pop();
            }

            // this is the blank slate reset that must be done
            // before the start of any single source calculation
            public boolean beforeGlobalCalculationReset() {
                singleSourceComputationComplete = false;
                currDistTo = NO_VALUE;
                childNodes = null;
                completedChildNodes = null;
                nextRoundChildNodes = null;
                allCompletedChildNodes.clear();
                seenEdges.clear();
                edgeTo = null;
                return true;
            }

            // this reset only happens for nodes that become Round Leaders
            // Round Leaders have extra data structures so they consume more resources
            public boolean roundLeaderReset() {
                regularNodePerRoundReset();
                roundComplete = false;
                if (nodeLeaderSingleNodeCalculationStack == null) {
                    nodeLeaderSingleNodeCalculationStack = new Stack<SourceRoundMessage>();
                }
                nodeLeaderSingleNodeCalculationStack.clear();
                if (childNodes == null) {
                    childNodes = new HashSet();
                }
                childNodes.clear();
                if (completedChildNodes == null) {
                    completedChildNodes = new HashSet<>();
                }
                completedChildNodes.clear();
                if (nextRoundChildNodes == null) {
                    nextRoundChildNodes = new HashSet<>();
                }
                nextRoundChildNodes.clear();
                return true;
            }

            //  this is the reset that happens when a non-single-source node
            //  becomes the round leader.
            //  Except for the beginning of the complete single-source computation
            //  this is executed on the other nodes in the graph that are NOT the single-source node
            public boolean regularNodePerRoundReset() {
                edgeNodeReset();
                nodeComplete = false;
                return true;
            }

            public boolean edgeNodeReset() {
                edgeNodes.clear();
                completedEdgeNodes.clear();
                edgeNodeNewEdgeCount = 0;
                nodeComplete = false;
                return true;
            }

            public boolean isRoundComplete() {
                return nodeLeaderSingleNodeCalculationStack.size() <= 0;
            }

            public void addEdgeNode(int i) {
                edgeNodes.add(i);
            }

            public boolean addCompletedEdgeNode(int i) {
                completedEdgeNodes.add(i);
                if (completedEdgeNodes.size() >= edgeNodes.size()) {
                    nodeComplete = true;
                }
                return nodeComplete;
            }

            public void addChildNode(int i) {
                if (childNodes == null) {
                    childNodes = new HashSet();
                }
                childNodes.add(i);
            }

            public void addCompletedChildNode(int i) {
                if (completedChildNodes == null) {
                    completedChildNodes = new HashSet();
                }
                completedChildNodes.add(i);
                allCompletedChildNodes.add(i);
                adjustChildNodes();
            }

            public String getChildNodeContents() {
                StringBuilder sb = new StringBuilder();
                sb.append("  completedChildNodes: {");
                if (completedChildNodes != null) {
                    for (Integer ii : completedChildNodes) {
                        sb.append(ii + ",");
                    }
                    sb.append("}\n");
                } else {
                    sb.append("}\n");
                }
                sb.append("  childNodes:          {");
                if (childNodes != null) {
                    for (Integer ii : childNodes) {
                        sb.append(ii + ",");
                    }
                    sb.append("}\n");
                } else {
                    sb.append("}\n");
                }
                sb.append("  nextRoundChildNodes:          {");
                if (nextRoundChildNodes != null) {
                    for (Integer ii : nextRoundChildNodes) {
                        sb.append(ii + ",");
                    }
                    sb.append("}\n");
                } else {
                    sb.append("}\n");
                }
                sb.append("  allCompletedChildNodes: {");
                if (allCompletedChildNodes != null) {
                    for (Integer ii : allCompletedChildNodes) {
                        sb.append(ii + ",");
                    }
                    sb.append("}\n");
                } else {
                    sb.append("}\n");
                }
                return sb.toString();
            }

            public void addNextChildNode(int i) {
                if (nextRoundChildNodes == null) {
                    nextRoundChildNodes = new HashSet();
                }
                nextRoundChildNodes.add(i);
            }

            public void addAllNextRoundChildNodes(Set<Integer> s) {
                if (s != null) {
                    for (Integer i : s) {
                        addNextChildNode(i);
                    }
                }
            }

            //  the hand off of the previous round leaders list of nextRoundChildNodes
            //  to become our childNodes list.
            public void receiveChildNodesFromPreviousRoundLeader(PathCalcElement other) {
                for (Integer i : other.allCompletedChildNodes) {
                    allCompletedChildNodes.add(i);
                }
                for (Integer i : other.completedChildNodes) {
                    allCompletedChildNodes.add(i);
                }
                for (Integer i : other.nextRoundChildNodes) {
                    childNodes.add(i);
                }
                adjustChildNodes();
            }

            // childNodes are the list of nodes that a node needs to process
            // if it turns out that any of these are in the list of completed nodes
            // then we take this opportunity to reconcile the lists
            // and remove the childNodes.
            // this can happen because of the nature of the distributed concurrent computation
            // there are only single live copies of the child nodes and completed child nodes at any time during a live batch
            // they live in the batch Round Leader node.
            public void adjustChildNodes() {
                if (allCompletedChildNodes != null) {
                    for (Integer i : allCompletedChildNodes) {
                        if (childNodes != null) {
                            if (childNodes.contains(i)) {
                                childNodes.remove(i);
                            }
                        }
                    }
                }
            }

            //  account for bidirectional Edges
            public boolean addSeenEdge(Edge e) {
                seenEdges.add(e);

                // BUG this breaks paths coming from the other way !
                Edge e2 = g.getOtherEdge(e);
                seenEdges.add(e2);
                return true;
            }

            //
            // An edge is no longer valid for consideration in a calculation
            // if the destination of the edge is to a completed node
            // because that means that this edge has already been considered for a finalized path segment
            // if the destination of the edge is not yet complete
            // then this edge is 'live'
            public boolean isEdgeLive(Edge e) {
                if (e == null) {
                    return false;
                }
                if (!seenEdges.contains(e)) {
                    return true;
                }
                if (!allCompletedChildNodes.contains(e.dest)) {
                    return true;
                }
                return false;
            }

            public int getSourceRoundLeader() {
                return sourceRoundLeader;
            }

            class RoundLeaderStateHolder {
                int globalNewEdgeCount;
                Set<Integer> nextRoundChildNodes = new HashSet();
                Set<Integer> completedChildNodes = new HashSet<>();
                Set<Integer> allCompletedChildNodes = new HashSet<>();

                RoundLeaderStateHolder(PathCalcElement e) {
                    globalNewEdgeCount = e.globalNewEdgeCount;
                    nextRoundChildNodes.addAll(e.nextRoundChildNodes);
                    completedChildNodes.addAll(e.completedChildNodes);
                    allCompletedChildNodes.addAll(e.allCompletedChildNodes);
                }

                public void transferToNew(PathCalcElement newPCE) {
                    newPCE.globalNewEdgeCount = globalNewEdgeCount;
                    newPCE.childNodes.addAll(nextRoundChildNodes);     // prev nextChild are next child
                    newPCE.allCompletedChildNodes.addAll(completedChildNodes);    // accumulate all completed
                    newPCE.allCompletedChildNodes.addAll(allCompletedChildNodes);   // retain all completed
                }
            }
        }

        class TableEntry {
            int sourceNode;
            int finalDestNode;
            int cost;
            int firstHopNode;

            TableEntry(int s, int t, int c, int f) {
                sourceNode = s;
                finalDestNode = t;
                cost = c;
                firstHopNode = f;
            }

            @Override
            public String toString() {
                return "[startNode(" + sourceNode + ")->finalDest(" + finalDestNode + ")].nextNode{" + firstHopNode + "}:totalCost(" + cost + ")";
            }
        }

        public class Message {
            final MessageType messageType;
            int from;
            int to;
            int cost;
            List<Integer> nodePath;
            List<Integer> pathCost;
            String message;

            Message(MessageType type, NetNode from, NetNode to) {
                this(type, from.netNodeNumber, to.netNodeNumber);
            }

            Message(MessageType type, int f, int t) {
                messageType = type;
                from = f;
                to = t;
                nodePath = new ArrayList<>();
                pathCost = new ArrayList<>();
            }

            public void addNodePath(int node) {
                nodePath.add(node);
            }

            public void addPathCost(int c) {
                pathCost.add(c);
            }

            @Override
            public String toString() {
                return messageType + " from: " + from + ", to: " + to + ". ";
            }

            public String printNodePath() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Integer i : nodePath) {
                    if (!first) {
                        sb.append("->");
                    }
                    first = false;
                    sb.append("(" + i + ")");
                }
                return sb.toString();
            }

            public String printPathCost() {
                StringBuilder sb = new StringBuilder();
                int total = 0;
                boolean first = true;
                for (Integer i : pathCost) {
                    if (!first) {
                        sb.append("+");
                    }
                    first = false;
                    total += i;
                    sb.append("[" + i + "]");
                }
                sb.append(" = " + total);
                return sb.toString();
            }
        }

        //
        //  General Source Round Calculation
        //
        public class SourceRoundMessage extends Message {
            final int calcSingleSourceNode;
            final int roundLeaderNode;

            public SourceRoundMessage(MessageType type, int s, int f, int t, int rleader) {
                super(type, f, t);
                calcSingleSourceNode = s;
                roundLeaderNode = rleader;
            }

            public int getCalcSingleSourceNode() {
                return calcSingleSourceNode;
            }

            public int getRoundLeaderNode() {
                return roundLeaderNode;
            }

            @Override
            public String toString() {
                return super.toString() + "singleSourceNode(" + calcSingleSourceNode + "), roundLeaderNode(" + roundLeaderNode + "). ";
            }
        }

        public class SourceRoundLeaderBeginCalculationMessage extends SourceRoundMessage {
            final PathCalcElement.RoundLeaderStateHolder previousRoundLeaderState;
            public SourceRoundLeaderBeginCalculationMessage(int s, int f, int t, int rleader,
                                                            PathCalcElement.RoundLeaderStateHolder rlsh
            ) {
                super(MessageType.SOURCE_ROUND_LEADER_BEGIN_CALCULATION, s, f, t, rleader);
                this.previousRoundLeaderState = rlsh;
            }
            PathCalcElement.RoundLeaderStateHolder getPreviousRoundLeaderState() {
                return previousRoundLeaderState;
            }
        }

        //
        //  This is a message sent by a previous round calc node to a target node for the current round
        //  It includes the cost of the source round and the tentative edge that connects
        //  the previous round node to the target node.
        //
        public class NodeEdgeCalculationMessage extends SourceRoundMessage {
            final int fromNodeCost;
            final Edge edgeFrom;

            public NodeEdgeCalculationMessage(int s, int f, int t, int rleader, int fc, Edge e) {
                super(MessageType.NODE_EDGE_CALCULATION, s, f, t, rleader);
                fromNodeCost = fc;
                edgeFrom = e;
            }
        }

        public class NodeEdgeCalculationCompleteMessage extends SourceRoundMessage {
            final Edge edgeFrom;
            final Set<Integer> nextRoundChildNodes;
            final int newEdgeCount;

            public NodeEdgeCalculationCompleteMessage(int s, int f, int t, int rleader, Edge e, Set<Integer> set, int nec) {
                super(MessageType.NODE_EDGE_CALCULATION_COMPLETE, s, f, t, rleader);
                edgeFrom = e;
                nextRoundChildNodes = set;
                newEdgeCount = nec;
            }
        }

        //
        //  Sent by calculating node back to the Source Round Leader Node
        //  After computation is complete.
        //  The nextNodes are the List of node numbers that this node has queued up dest edges for
        //  for the next round of computation
        //
        public class SingleNodeCalculationsCompleteMessage extends SourceRoundMessage {
            final Set<Integer> nextNodes;
            final int newEdgeCount;

            public SingleNodeCalculationsCompleteMessage(int s, int f, int t, int rleader, Set<Integer> li, int c) {
                super(MessageType.SINGLE_NODE_CALCULATIONS_COMPLETE, s, f, t, rleader);
                nextNodes = li;
                newEdgeCount = c;
            }

            public Set<Integer> getNextNodes() {
                return nextNodes;
            }

            public int getNewEdgeCount() {
                return newEdgeCount;
            }

            @Override
            public String toString() {
                return messageType + " newEdgeCount=" + newEdgeCount;
            }
        }
    }


    public class Edge {
        final public int source;
        final public int dest;
        final public int value;

        public Edge(int v, int w, int val) {
            this.source = v;
            this.dest = w;
            this.value = val;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Edge)) {
                return false;
            }
            Edge otherEdge = (Edge) other;
            if (!(otherEdge.source == source)) {
                return false;
            }
            if (!(otherEdge.dest == dest)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return source + dest;
        }

        @Override
        public String toString() {
            return "[" + source + "-" + dest + "](" + value + ")";
        }
    }

    /**
     * This is a undirected graph representing bidirectional network connections
     */
    public class GraphAdjList {
        protected int lowestVertexNumber = NO_VALUE;     // for the START node
        protected int highestVertexNumber = -1;
        Set<Integer> vertices = new HashSet<>();
        protected int sizeLimit;
        protected int vCount;
        protected int eCount;
        protected boolean isDigraph = true;    // is directed graph
        protected LinkedNode[] adj;
        protected boolean[] marked;    // generic vertex marking.  use where needed

        public GraphAdjList() {
            this(1000);
        }

        public GraphAdjList(int sizeLimit) {
            this.sizeLimit = sizeLimit;
            adj = new LinkedNode[sizeLimit];
        }

        public int vCount() {
            return vCount;
        }

        public int eCount() {
            return eCount;
        }

        public int getHighestVertexNumber() {
            return highestVertexNumber;
        }

        public synchronized int insert(Edge e) {
            if (getEdge(e.source, e.dest) != null) {
                return eCount;
            }
            int v = e.source;
            int w = e.dest;

            // track the highest vertex number that the graph has
            if (v > highestVertexNumber) highestVertexNumber = v;
            if (w > highestVertexNumber) highestVertexNumber = w;
            if (v < lowestVertexNumber) lowestVertexNumber = v;           // must be a start node
            if (!vertices.contains(v)) {
                vCount++;
                vertices.add(v);
            }
            if (!vertices.contains(w)) {
                vCount++;
                vertices.add(w);
            }
            // handle registered vertex with no edge.
            // insert a self-edge if there is not already one
            // link new node into the head of the list
            adj[v] = new LinkedNode(v, e, adj[v]);
            return ++eCount;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < adj.length; i++) {
                LinkedNode n = adj[i];
                if (n != null) {
                    sb.append("v=" + i + ": edges: ");
                    while (n != null) {
                        sb.append(n.edge().toString()).append(", ");
                        n = n.next();
                    }
                    sb.append("\n");
                }
            }
            return sb.toString();
        }

        public LinkedNode getAdjList(int v) {
            return adj[v];
        }

        public Edge getEdge(int v, int w) {
            LinkedNode n = adj[v];
            if (n == null) {
                return (Edge) null;
            }
            if (n.w == w) {
                return n.e;
            }
            while (n.next != null) {
                n = n.next;
                if (n.w == w) {
                    return n.e;
                }
            }
            return (Edge) null;
        }

        public Edge getOtherEdge(Edge e) {
            if (e == null) {
                return null;
            }
            if (getEdge(e.source, e.dest) == null) {
                return null;
            }
            return getEdge(e.dest, e.source);
        }

        public int getEdgeValue(int v, int w) {
            Edge e = getEdge(v, w);
            if (e != null) {
                return e.value;
            }
            return NO_VALUE;
        }

        protected Edge newEdge(int i, int j, int value) {
            return new Edge(i, j, value);
        }

        protected String printNodeList(LinkedNode n) {
            if (n == null) return "NULL";
            StringBuilder sb = new StringBuilder();
            sb.append("vertex list for " + n.vertexTailNumber() + ": ");
            sb.append(n.vertexHeadNumber()).append(", ");
            while (n.hasNext()) {
                n = n.next();
                sb.append(n.vertexHeadNumber()).append(", ");
            }
            return sb.toString();
        }

        protected String printEdgeList(List<Edge> e) {
            if (e == null) return "";
            StringBuilder sb = new StringBuilder();
            sb.append("edge list: ");
            for (Edge edge : e) {
                sb.append(e.toString()).append(", ");
            }
            return sb.toString();
        }
    }

    public class LinkedNode {
        final int v;       // tail vertex number
        final int w;       // head vertex number
        final Edge e;
        LinkedNode next;

        public LinkedNode(int v, Edge e, LinkedNode next) {
            this.v = v;
            this.e = e;
            this.w = e.dest;
            this.next = next;
        }

        public LinkedNode next() {
            return this.next;
        }

        public void setNext(LinkedNode n) {
            next = n;
        }

        public boolean hasNext() {
            return (next != null);
        }

        public int vertexHeadNumber() {
            return w;
        }

        public int vertexTailNumber() {
            return v;
        }

        public Edge edge() {
            return e;
        }

        public boolean isSelfEdge() {
            return e.dest == e.source;
        }

        public int length() {
            int len = 1;
            LinkedNode n = next();
            while (n != null) {
                len++;
                n = n.next();
            }
            return len;
        }

        public String printNodeChain() {
            StringBuilder sb = new StringBuilder();
            LinkedNode n = this;
            while (n != null) {
                sb.append(n.vertexTailNumber() + "-" + n.vertexHeadNumber()).append(", ");
                n = n.next();
            }
            return sb.toString();
        }
    }

    public static String demoIntro() {
        return "This Demo simulates a network in which new members or connections can be added at any time\n" +
                " * \n" +
                " * The primary point of the demo is to show that as new nodes are added, the most cost-efficient\n" +
                " * way to route a message between nodes is updated.\n" +
                " * The harder problem of dealing with nodes that drop out is ignored and out of scope of this simple demo\n" +
                " * whose primary purpose is to show the use of a distributed Bellman-Ford graph single source shortest paths algorithm.\n" +
                " * \n" +
                " * Simulating the decentralized nature of the internet the routing information and calculation is\n" +
                " * distributed to the nodes.\n" +
                " * \n" +
                " * Minimum cost routing from node to node is computed using distributed Bellman-Ford for each network node.\n" +
                " * This is a high cost way of computing All Pairs Shortest Paths but the benefit is that it is decentralized.\n" +
                " * Each node controls the distributed calculation of the minimum paths to itself.\n" +
                " * Because of the way that Bellman-Ford Dynamic Programming approach works, the progress of the calculation\n" +
                " * depends on the computation proceeding in being discrete steps.\n" +
                " * \n" +
                " * This is synchronous distributed algorithm progress.\n" +
                " * One step must complete before the next step begins.\n" +
                " * \n" +
                " * In the general case a network might elect a leader to act as the administrator that coordinates the beginning\n" +
                " * and ending of a discrete step.  A consensus algorithm might be used to guarantee that every node agrees upon\n" +
                " * which node is the administrator so that there is only one.\n" +
                " * \n" +
                " * In our case we can dispense with the overhead and complexity of a consensus algorithm by taking advantage\n" +
                " * of the parallel but sequential nature of the algorithm.\n" +
                " * \n" +
                " * We process nodes in synchronous batches in which the next batch of nodes to be processed are those that are\n" +
                " * reachable from the nodes of the current batch.\n" +
                " * The actual algorithm involves the edges between the nodes but we use the nodes as the primary objects\n" +
                " * that the implementation controls.\n" +
                " * \n" +
                " * A synchronous batch is complete after the last unprocessed node has been used in a calculation.\n" +
                " * For each batch there is a 'Leader Node'.\n" +
                " * The Leader Node knows what tne next set of nodes are from which the next set of edges will come from.\n" +
                " * The batch calculation is done concurrently with each node tracking when it has finished computing\n" +
                " * all of its new edges.  After a node completes it reports back to the Leader Node of its completion status.\n" +
                " * When the Leader Node receives the completion notice from the last remaining non-complete node\n" +
                " * then it knows that the current batch is complete and that is it time to kick off the next batch.\n" +
                " * There is a next batch if there are still more nodes that have not been reached by previous batches.\n" +
                " * \n" +
                " * When it is time for the next batch to begin, the first node arbitrarily plucked from the Set next nodes becomes\n" +
                " * the designated next Leader Node.\n" +
                " * The Leader Node ID once chosen is propagated to forward to all participants in the cascade of network\n" +
                " * messages that coordinate the next batch of calculations.\n\n" +
                " * This can be a highly concurrent program.  The computation of the network graph can be very expensive.\n" +
                " * There is a delay constant built in to allow time for the calculations to complete before the demo tries\n" +
                " * use the network to send a message.   Depending on your machine this may not be enough time.\n" +
                " * If a non-optimal path is selected try increasing the time: GLOBAL_ROUTE_POST_COMPUTE_SLEEPTIME.\n\n" +
                " *   If you want DEBUG information showing the distributed computation set -DDEBUG_PROTOCOL on the command line" +
                " * \n\n";
    }

    public static void runDemo0() {
        cons(demoIntro());
        cons("==================================\n  B E G I N    D E M O\n\n");

        String debug_protocol = System.getProperty("DEBUG_PROTOCOL");

        InternetRoutingSimulation prog = new InternetRoutingSimulation();
        if (debug_protocol != null) {
            prog.DEBUG_PROTOCOL = true;
        }


        NetNode n1 = prog.createNewNetNode();
        cons("created NetNode: " + n1.getName());

        NetNode n2 = prog.createNewNetNode();
        cons("created NetNode: " + n2.getName());

        int cost = 10;
        cons("add new connection " + n1.getName() + "<--> " + n2.getName() + " cost: " + cost + "\n");
        prog.newConnection(n1.netNodeNumber, n2.netNodeNumber, cost);
        prog.WAIT_GRAPH_OPEN();

        String m = "This is a simple demo between our new direction connection between 2 nodes. Message from " + n1.getName() + "to " + n2.getName();
        NetNode.SourceRoundMessage message = n1.new SourceRoundMessage(MessageType.SEND_MESSAGE, 0, n1.netNodeNumber, n2.netNodeNumber, 0);
        message.message = m;
        cons("send a message from " + n1.getName() + "to " + n2.getName() + "  '" + m + "'");
        n1.send(message);

        NetNode n3 = prog.createNewNetNode();
        cons("created NetNode: " + n3.getName());

        cost = 2;
        cons("add new connection " + n3.getName() + "<--> " + n2.getName() + " with cost: " + cost + "\n");
        prog.newConnection(n3.netNodeNumber, n2.netNodeNumber, cost);
        prog.WAIT_GRAPH_OPEN();


        NetNode n4 = prog.createNewNetNode();
        cons("created NetNode: " + n4.getName());
        cost = 9;
        cons("add new connection " + n3.getName() + "<--> " + n4.getName() + " with cost: " + cost + "\n");
        prog.newConnection(n3.netNodeNumber, n4.netNodeNumber, cost);
        prog.WAIT_GRAPH_OPEN();

        m = "This is a Message from " + n1.getName() + "to " + n4.getName() +
                ".  It will take the only available path through the connections that we have made so far.";
        cons("send a message from " + n1.getName() + "to " + n4.getName() + "  '" + m + "'");
        message = n1.new SourceRoundMessage(MessageType.SEND_MESSAGE, 0, n1.netNodeNumber, n4.netNodeNumber, 0);
        message.message = m;
        n1.send(message);
        prog.WAIT_GRAPH_OPEN();

        cost = 20;
        cons("add new connection " + n1.getName() + "<--> " + n4.getName() + " with cost: " + cost + "\n");
        prog.newConnection(n1.netNodeNumber, n4.netNodeNumber, cost);
        prog.WAIT_GRAPH_OPEN();

        m = "This is another Message from " + n1.getName() + "to " + n4.getName() +
                ".  Note that it will take the new lowest cost direct path that we added via the new connection.";
        cons("send a message from " + n1.getName() + "to " + n4.getName() + "  '" + m + "'");
        message = n1.new SourceRoundMessage(MessageType.SEND_MESSAGE, 0, n1.netNodeNumber, n4.netNodeNumber, 0);
        message.message = m;
        n1.send(message);
        prog.WAIT_GRAPH_OPEN();

        cost = 1;
        cons("add new connection " + n2.getName() + "<--> " + n4.getName() + " with cost: " + cost + "\n");
        n2.addNewConnectionTo(n4.netNodeNumber, cost);
        prog.WAIT_GRAPH_OPEN();


        m = "This is yet another Message from " + n1.getName() + "to " + n4.getName() +
                ".  Note that compared to the previous message, this one it will take more network hops but at a lower cost.";
        cons("send a message from " + n1.getName() + "to " + n4.getName() + "  '" + m + "'");
        message = n1.new SourceRoundMessage(MessageType.SEND_MESSAGE, 0, n1.netNodeNumber, n4.netNodeNumber, 0);
        message.message = m;
        n1.send(message);
        prog.WAIT_GRAPH_OPEN();

        cons("\n\n     D E M O   E N D\n==================================\n\n\n");
    }

    public static void main(String[] args) {
        runDemo0();
    }
}
