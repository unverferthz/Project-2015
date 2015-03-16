using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SensorDisplay
{
    class DistanceDisplayManager
    {
        //Global variables
        Graphics canvas;
        Pen p;

        int startX;
        int startY;

        public DistanceDisplayManager(Graphics canvas)
        {
            //Set up everything
            this.canvas = canvas;
            p = new Pen(Color.Red);
            p.Width = 3;
            startX = 400;
            startY = 50;
        }

        //Draw in the data points passed through
        public void Draw(int[] dataToDisplay)
        {
            //Clear out old lines
            canvas.Clear(Color.White);

            int currX = startX;

            //Loop through data points
            for(int i=0; i < dataToDisplay.Length - 1; i++)
            {

                int firstPointY = 0;
                int secondPointY = 0;

                //If the number is greater than 100 then just max it out at 100
                if (dataToDisplay[i] > 100)
                    //Multiply by 3 to just spread data out more
                    firstPointY = 100 * 3;
                else
                    //If not greater than 100 then just use the numer that's in there by the multiplier
                    firstPointY = (dataToDisplay[i] * 3) + startY;

                //If the number is greater than 100 then just max it out at 100
                if (dataToDisplay[i+1] > 100)
                    //Multiply by 3 to just spread data out more
                    secondPointY = 100 * 3;
                else
                    //If not greater than 100 then just use the numer that's in there by the multiplier
                    secondPointY = (dataToDisplay[i+1] * 3) + startY;

                //Draw line between 2 points
                Point firstPoint = new Point(currX, startY + firstPointY);
                Point secondPoint = new Point(currX + 20, startY + secondPointY);
                canvas.DrawLine(p,firstPoint, secondPoint);

                //Move X across for next point
                currX += 20;
            }
        }
    }
}
