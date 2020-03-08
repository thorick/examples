/*
  https://www.codewars.com/kata/largest-pair-sum-in-array/train/javascript

Given an array of integers, find the largest sum of any pair of values in the array.


 */
/*
   Array based heap..
   binary tree
   parent is greater than any generation of children below it
   root node a array position 1

--------------------------------------------------
NO :
   add new value starting at root top element
   if LT at that pos
   then choose the largest child
   do ladder of swaps until at a position where element is larger than both children at that position
   repeat as necessary
----------------------------------------------------

  it's easier to add the new element at the TAIL because
  we already know where that is and it's easier to add at the
  tail and swap upwards.
  If intert at the HEAL/root you have to keep an intermediate place
  and do a shove into the array from the middle


   children of parent at N
      left child:
        N * 2
      right child
       (N * 2) + 1

   parent of child at N
      parent:  N / 2   (ignore remainder of 1 from right child

G:\b\notes\java\my_examples\src\main\javascript\datastructrures\heap>cp ArrayHeap.js.java ArrayHeap_02.js

*/
var printTrace = true;
var heapSize = 5;
var currNextInsertIndex = 1; // add the next new element here
var arraySize = 0;
var heap = [
];
function P(message) {
  if (!printTrace) {
    return;
  }
  console.log(message);
  //alert(message);
}


function initializeArrayToSize(size) {
  arraySize = size;
  heapSize = arraySize + 1;
  P('heap size has been set to=' + heapSize);
  var position;
  for (position = 0; position < heapSize; position += 1) {
    heap[position] = 0;
  }
  currNextInsertIndex = 1;
}

function swap(topIndex, bottomIndex) {
  var tempValue = heap[topIndex];
  heap[topIndex] = heap[bottomIndex];
  heap[bottomIndex] = tempValue;
}
function swapUpUntil(index) {
  if (index <= 1) {
    return;
  }
  var currIndex = index;
  var currValue = heap[index];
  var parentIndex = Math.trunc(index / 2);
  var parentValue = heap[parentIndex];
  while (currValue > parentValue) {
    P('TOP of swapUpUntil while.  currValue: child heap[' + index + ']=' + currValue + ' > parent heap[' + parentIndex + ']=' + parentValue + ' do swap');
    swap(parentIndex, currIndex);
    currIndex = parentIndex;
    P('swapped with parent we are the new parent with new currIndex=' + currIndex + ' and its new value heap[' + currIndex + ']=' + heap[currIndex]);
    if (currIndex <= 1) {
      return;
    }
    parentIndex = Math.trunc(currIndex / 2);
    P(' new parentIndex=' + parentIndex + ', heap[' + parentIndex + ']=' + heap[parentIndex]);
  }
}


// swap with child until we are larger than our children
// add new entry to the end of the array (expand the live values)
// and swap up until we are smaller than our parent and hold there

function putNew(value) {
  var index = currNextInsertIndex;
  currNextInsertIndex++;
  heap[index] = value;
  P('putNew: add value to end of array at index=' + index + '  heap[' + index + ']=' + value + ' , bumped currNextInsertIndex=' + currNextInsertIndex);
  if (index <= 1) {
    P('done  inserted at the top of the heap array at index 1');
    Pheap();
    return;
  }
  var parentIndex = Math.trunc(index / 2);
  P(' child heap[' + index + '] value=' + value + ' parent heap[' + parentIndex + '] = ' + heap[parentIndex]);
  if (value > heap[parentIndex]) {
    P(' do swapUpUNtil from child heap[' + index + ']=' + value);
    swapUpUntil(index);
  }
  else {
    P('child value heap[' + index + ']=' + value + '  is <  parent: heap[' + parentIndex + '] = ' + heap[parentIndex] + ', so we leave value in place in heap');
  }
}


// pull the top elment, the largest in the tree
//
// then take the last element at the TAIL put it at the head (shrink the tree)
// swap down until we are smaller that our new parent
//  return the top element
//

function poll() {
  if (currNextInsertIndex <= 1) {
    return null;
  }
  var topElement = heap[1];
  var tailIndex = currNextInsertIndex - 1;
  heap[1] = heap[tailIndex];
  currNextInsertIndex = currNextInsertIndex - 1; // you MUST be sure to do this. it's the live array boundary
  swapDownUntil(1);
  return topElement;
} //  swap places with our parent

function swapDownUntil(index) {
  if (currNextInsertIndex <= 1) {
    return;
  } // we're the only and the top

  var currIndex = index;
  var currValue = heap[currIndex];
  var leftChildIndex = index * 2;
  var rightChildIndex = (index * 2) + 1;
  var leftChildValue = Number.MIN_SAFE_INTEGER;
  var rightChildValue = Number.MIN_SAFE_INTEGER;
  // array range check
  if (leftChildIndex < currNextInsertIndex) {
    leftChildValue = heap[leftChildIndex];
  } // array range check

  if (rightChildIndex < currNextInsertIndex) {
    rightChildValue = heap[rightChildIndex];
  } //
  // we're still less than a child so we swap
  //

  while ((leftChildValue || rightChildValue) > currValue) {
    if (leftChildValue > rightChildValue) {
      swap(currIndex, leftChildIndex);
      currIndex = leftChildIndex;
    }
    else {
      swap(currIndex, rightChildIndex);
      currIndex = rightChildIndex;
    } // set up for next child swap down

    currValue = heap[currIndex];
    leftChildIndex = currIndex * 2;
    rightChildIndex = (currIndex * 2) + 1;
    leftChildValue = Number.MIN_SAFE_INTEGER;
    rightChildValue = Number.MIN_SAFE_INTEGER;
    //leftChildValue = heap[leftChildIndex];
    //rightChildValue = heap[rightChildIndex];
    if (leftChildIndex < currNextInsertIndex) {
      leftChildValue = heap[leftChildIndex];
    }
    if (rightChildIndex < currNextInsertIndex) {
      rightChildValue = heap[rightChildIndex];
    }
  }
}
function setHeap(array) {
  var arraySize = array.length;
  initializeArrayToSize(arraySize);
  P('setHeap  now do putNew on the input array values');
  for (position = 0; position < arraySize; position++) {
    P(' setHeap:  do putNew for array position=' + position + '  value=' + array[position]);
    putNew(array[position]);
  }
}
function Pheap() {
  Parray(heap);
}
function printArray(array) {
  var position;
  var s = ' ';
  for (position = 0; position < heapSize; position += 1) {
    var s1 = 'a[' + position + '] = ' + array[position] + ', ';
    s = s + s1;
  }
  return s;
}
function printLinearArray(array) {
  var position;
  var s = '[';
  var first = true;
  for (position = 0; position < heapSize; position += 1) {
     var s1 = array[position];
     if (!first) {
        s1 = ', ' + s1;
     }
     first = false;
     s = s + s1;
   }
   s = s + ']';
   return s;
}
function Parray(array) {
  s = printArray(array);
  P(s);
}
function largestNSum(numbers, sumOfLargestNsize)
{
  debugger;
  setHeap(numbers);
  var sum = 0;
  for (count = 1; count <= sumOfLargestNsize; count++) {
    var topVal = poll();
    P('largestNSum:  after poll() topVal='+topVal+', sum before add='+sum);
    sum = sum + topVal;
    P('largestNSum:  after add of topVal, sum='+sum);
  }
  return sum;
}
function largestPairSum(numbers)
{
  return largestNSum(numbers, 2);
}

function run_demo()
{
  var input = [-10, -8, -16, -18, -19];
  //var input = [1, 99, 3, 11, 10];
  //var input = [-9, -2, -7, -1];
  var inputAsString = printLinearArray(input);
  var result = largestPairSum(input);
  alert('Demo results:  for input array '+inputAsString+'\n\nthe sum of the largest 2 elements is '+result);
}

function run_user_input_demo() {
  var n1 = Number(window.prompt('An example of a Heap implemented as an array in javascript.\n\nYou will Enter 5 numbers.\n' +
                          'You will Enter how many of the largest of those numbers to add together.\n\n' +
                          'This is implemented by adding each number to a Heap/Priority Queue.\n'+
                          'The Heap maintains the highest values at its top so we just pull the first two entries off of the Heap and add them.\n\n'+
                          'The Heap Datastructure is implemented in javascript.\n'+
                          'You can see the js code loaded with this webpage as ArrayHeap.js\n\n'+
                          'Enter Number 1: '));
  var n2 = Number(window.prompt('Enter Number 2: '));
  var n3 = Number(window.prompt('Enter Number 3: '));
  var n4 = Number(window.prompt('Enter Number 4: '));
  var n5 = Number(window.prompt('Enter Number 5: '));
  var howMany = Number(window.prompt('How many of the largest numbers do you want to sum up ?  (min=1, max=5): '));
  if (howMany < 1 || howMany > 5) {
    alert('The number you entered is out of the range 1 to 5, so behavior or results not guaranteed !');
  }
  input = [n1, n2, n3, n4, n5];
  var inputAsString = printLinearArray(input);
  var result = largestNSum(input, howMany);
  alert('Demo results:  for input array '+inputAsString+'\n\nthe sum of the largest '+howMany+' elements is: '+result+"\n\nDONE !");
}
/////////////////////////////////////////
//  stop here
/////////////////////////////////////////



/*
/////////////////////////////
//  some dev testing to keep around
//
function runTest() {
  P('run test now');
  P('start with iknitialize');
  initializeArrayToSize(15);
  Pheap();
  putNew(5);
  P(' do putNew(4)');
  putNew(4);
  Pheap();
  P(' do putNew(1)');
  putNew(1);
  Pheap();
  P(' do putNew(10)');
  putNew(10);
  Pheap();
  putNew(11);
  Pheap();
  var value = poll();
  P('poll value=' + value);
  Pheap();
  var value = poll();
  P('poll value=' + value);
  Pheap();
  var value = poll();
  P('poll value=' + value);
  Pheap();
  var value = poll();
  P('poll value=' + value);
  Pheap();
}
function test02() {
  var array = [
    - 10,
    - 8,
    - 2
  ];
  setHeap(array);
  var value = poll();
  P('poll value=' + value);
  Pheap();
  P('poll value=' + value);
  Pheap();
}

runTest();
test02();
//
/////////////////////////////////////////
*/
