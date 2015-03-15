using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SensorDisplay
{
    class InputDataManager
    {
        int[] inputData;
        private int numInArray;
        private int maxInArray;

        public InputDataManager(int numEntriesHeld)
        {
            maxInArray = numEntriesHeld;
            inputData = new int[maxInArray];
            numInArray = 0;
        }

        public void resetArray()
        {
            inputData = new int[maxInArray];
        }

        public void addData(int newNumber)
        {
            //If array is full
            if (numInArray == maxInArray)
            {
                makeRoomInArray();
                inputData[maxInArray - 1] = newNumber;
            }
            else
            {
                inputData[numInArray] = newNumber;
                numInArray++;
            }
            
        }

        public int[] getArray()
        {
            return inputData;
        }

        private void makeRoomInArray()
        {
            //Move every number down one in the array, knocking out the number in slot 0
            for(int i=1; i < inputData.Length; i++)
            {
                inputData[i - 1] = inputData[i];
            }
        }


    }
}
