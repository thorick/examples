
'''
 * Binary Search Tree in which each Node is augmented with information about relatively how
 * frequently the node is accessed.
 * <p>
 * There are numNodes  ordered nodes
 * <p>
 * There is a subproblem C(i,j)  for  0 <= i <= j <= numNodes
 * C(i,j)
 *
 * For each subproblem we want to find which configuration yields the minimum cost for which node that is placed at the root
 * That configuration is chosen as the value to assign to C(i.j)
 *
 * So one at a time we choose a candidate root node starting from the left side of the interval at i up until the right
 * side of the interval at j.
 *
 * Each choice of root divides the the subgraph into 2 subtrees each of which have been evaluated in the same way.
 *
 * The candidate value is:   Sum(frequency of each node i to j) + C(i, r-1) + C(r+1, j)
 *
 * The Sum(frequency of each node i to j)
 *     The Sum of all probabilities is how many times the root node will be visited while seeking out each node at their specified frequency.
 *     Likewise for each subtree which happens to be the optimal subtree we will get how many times that subroot is accessed for each node in  its subtree.
 *
 * <p>
 * In order for DP to work all of the smallest subproblems must be done first.
 * This is so that the shortest subtree costs have already been computed and are available for future C(i,j) subproblems.
 * So this is a traversal by SIZE not strictly by the value of the indexes i or j.
 * It is the size of subtrees spanning the indexes i and j in order.
 * The final answer is the cost of the complete set of nodes numNodes.
 * <p>
 * <p>
 * We'll load an ordered set of nodes with integer values, each will have a search count associated with it, unscaled for simplicity
 * <p>
 * each node takes 2 array position:   0 value  1 frequency
 * so node pos is index * 2    frequence pos is  (index * 2) + 1
 * <p>
 * each tree entry takes 2 array positions for children:  0  LEFT  1 RIGHT
 * node LEFT CHILD pos  is index * 2  node RIGHT CHILD pos is  (index * 2) + 1

'''
import sys
from _tracemalloc import start
from unittest.mock import right


class OptimalBinarySearchTree:
    def __init__(self, orderedNodes):
        self.orderedNodes = orderedNodes
        self.numNodes = int(len(orderedNodes) / 2)
    
    def frontPad(self, inStr, length):
        diff = length - len(inStr)
        if diff <= 0:
            return inStr
        return (" " * diff) + inStr
      
    #  any negative numbers are printed as 'x'
    def printPadded2DIntArray(self, a):
        rowLen = len(a)
        colLen = len(a[0])
        temp = colLen
        maxColLen = 0
        while temp > 0:
            temp /= 10
            temp = int(temp)
            maxColLen += 1
        
        if maxColLen > 0:
            frontBlankRowPad = " " * maxColLen
        else:
            frontBlankRowPad = ""
           
        maxSize = 0
        rowInd = 0
        while rowInd < rowLen:
            colInd = 0
            while colInd < colLen:
                val = a[rowInd][colInd]
                if  val > maxSize:
                    maxSize = val
                colInd += 1
            rowInd += 1
          
        numSize = 1
        while maxSize > 9:
            maxSize /= 10
            maxSize = int(maxSize)
            numSize += 1
            
        colInd = colLen - 1
        while colInd >= 0:
            outStr = ""
            if colInd % 5 == 0:
                outStr = self.frontPad(str(colInd), maxColLen)
            else:
                outStr = frontBlankRowPad
            outStr = outStr + "  "
            rowInd = 0
            while rowInd < rowLen:
                val = a[rowInd][colInd]
                if val < 0:
                    val = 'x'
                outStr = outStr + "[" + self.frontPad(str(val), numSize) + "] "
                rowInd += 1
            print(outStr)
            colInd -= 1
            
        outStr = frontBlankRowPad
        outStr = outStr + "  "
        rowInd = 0
        while rowInd < rowLen:
            outStr = outStr + " " + self.frontPad(str(rowInd), numSize) + "  "
            rowInd += 1
        print(outStr)
            
        
              
    def printNodeArray(self, list):
        i = 0
        while i < len(list)/2:
            valIndex = i * 2
            freqIndex = (i * 2) + 1
            print("("+str(i)+")  [val="+str(list[valIndex])+", freq="+str(list[freqIndex])+"]")
            i = i + 1
            
    def sumFreq(self, leftIndex, rightIndex):
        total = 0
        index = leftIndex
        while index <= rightIndex:
            total += self.orderedNodes[(index * 2)+1]
            index += 1
        return total
        
    
    def doMinCsubProblem(self, size, startIndex):
        minVal = sys.maxsize
        minRootIndex = -1
        leftIndex = startIndex
        rightIndex = startIndex + size - 1
        sumFreq = self.sumFreq(leftIndex, rightIndex)
        
        # try every position for the root along the contiguous index range
        rootIndex = leftIndex
        while rootIndex <= rightIndex:
            tempVal = sumFreq
            if rootIndex == leftIndex:
                tempVal += self.min_results[leftIndex+1][rightIndex]
            elif rootIndex == rightIndex:
                tempVal += self.min_results[leftIndex][rightIndex-1]
            else:
                tempVal += self.min_results[leftIndex][rootIndex-1]
                tempVal += self.min_results[rootIndex+1][rightIndex] 
            if tempVal < minVal:
                minVal = tempVal
                minRootIndex = rootIndex 
            rootIndex += 1
                 
        self.min_results[leftIndex][rightIndex] = minVal
        self.chosen_r[leftIndex][rightIndex] = minRootIndex 
            
           
        
        
        
        
        
        
    def compute(self):
        print("compute !")
        print("input list: "+str(self.orderedNodes))
        print("numNodes="+str(self.numNodes))
        print("input nodes: ")
        self.printNodeArray(self.orderedNodes)
        freqSum03 = self.sumFreq(0, 3)
        print("sum freqs  0:3 = "+str(freqSum03))
        
        self.min_results = [[0 for i in range(self.numNodes)] for j in range(self.numNodes)]
        self.chosen_r    = [[0 for i in range(self.numNodes)] for j in range(self.numNodes)]
        
        # initialize the root only diagonal:  root with no children
        i = 0
        while i < self.numNodes:
            self.min_results[i][i] = self.sumFreq(i, i)
            i += 1
        
        #print("now print min_results inited")
        #self.printPadded2DIntArray(self.min_results)
        #print("DONE\n")
        
        # outer loop controls the size of the subtree that we are computing
        size = 2
        while size <= self.numNodes:
            # print("begin size="+str(size))
            startIndex = 0
            while startIndex <= (self.numNodes - size):
                # print("   begin startIndex="+str(startIndex))
                self.doMinCsubProblem(size, startIndex)
                startIndex += 1
            size += 1
            
        print("compute DONE.")
        print("\nfinal main value matrix: ")
        self.printPadded2DIntArray(self.min_results)
        print("\nfinal chosen root matrix: ")
        self.printPadded2DIntArray(self.chosen_r)
        
        resultTree = self.constructResultGraph()
        print("\nfinal tree graph adjacency matrix where root node is A[0]["+str(self.numNodes-1)+"] = "+str(self.chosen_r[0][self.numNodes-1]))
        print("  row 0 is LEFT child   row 1 is RIGHT child")
        self.printPadded2DIntArray(resultTree)
        print("\n\nDONE.")
    
    def processRoot(self, graphAdjList, visited, currRoot_ij, parent_ij):
        currNode = self.chosen_r[currRoot_ij[0]][currRoot_ij[1]]
        
        # if we're not the top of the tree then register us with our parent
        if parent_ij[0] >= 0:
            parentNode = self.chosen_r[parent_ij[0]][parent_ij[1]]
            if currNode < parentNode:
                # left child
                graphAdjList[parentNode][0] = currNode
            else:
                # right child
                graphAdjList[parentNode][1] = currNode
                
        visited[currNode] = True
        
        # now handle our left subtree if there is one
        if currNode > 0:
            # find the breadth of our left subtree go left until we've reached an already visited node
            nextNode = currNode - 1
            count = 0
            while nextNode >= 0:
                if visited[nextNode]:
                    break
                count += 1
                nextNode -= 1
                
            # now determine our subtree indexes
            rightIndex = currNode - 1
            leftIndex = currNode - count
            size = rightIndex - leftIndex
            if size == 0:
                # we've reached the leaf level
                visited[leftIndex] = True
                graphAdjList[currNode][0] = leftIndex
            elif size > 0:
                # process the left subtree
                nextRoot_ij = [leftIndex, rightIndex]
                self.processRoot(graphAdjList, visited, nextRoot_ij, currRoot_ij)
                
        # now handle our right subtree if there is one
        if currNode < self.numNodes - 1:
            # find the breadth of our right subtree go right until we've reached an already visited node
            nextNode = currNode + 1
            count = 0
            while nextNode < self.numNodes:
                if visited[nextNode]:
                    break
                count += 1
                nextNode += 1
                
            # now determine our subtree indexes
            leftIndex = currNode + 1
            rightIndex = currNode + count
            size = rightIndex - leftIndex
            if size == 0:
                # we've reached the leaf level
                visited[rightIndex] =  True
                graphAdjList[currNode][1] = rightIndex
            elif size > 0:
                nextRoot_ij = [leftIndex, rightIndex]
                self.processRoot(graphAdjList, visited, nextRoot_ij, currRoot_ij)
                
        return True
    
    
     
    def constructResultGraph(self):
        graphAdjList = [[-1 for i in range(2)] for j in range(self.numNodes)]
        visited = [False for i in range(self.numNodes)]
        parent_ij = [-1, -1]
        currRoot_ij = [0, self.numNodes-1]
        done = False
        while done == False:
            done = self.processRoot(graphAdjList, visited, currRoot_ij, parent_ij)
        return graphAdjList
        
        
        
orderedNodeList = [0,1, 1,4, 2,10, 3,1, 4,7, 5,3]
optProgram = OptimalBinarySearchTree(orderedNodeList)
optProgram.compute()


    
