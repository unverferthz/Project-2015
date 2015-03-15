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
        Graphics canvas;
        Pen p;

        int startX;
        int startY;

        public DistanceDisplayManager(Graphics canvas)
        {
            this.canvas = canvas;
            p = new Pen(Color.Red);

            startX = 400;
            startY = 50;
        }

        public void Draw(int[] dataToDisplay)
        {
            canvas.Clear(Color.White);

            int currX = startX;

            for(int i=0; i < dataToDisplay.Length - 1; i++)
            {

                int firstPointY = 0;
                int secondPointY = 0;

                if (dataToDisplay[i] > 100)
                    firstPointY = 100 * 3;
                else
                    firstPointY = (dataToDisplay[i] * 3) + startY;

                if (dataToDisplay[i+1] > 100)
                    secondPointY = 100 * 3;
                else
                    secondPointY = (dataToDisplay[i+1] * 3) + startY;

                Point firstPoint = new Point(currX, startY + firstPointY);
                Point secondPoint = new Point(currX + 20, startY + secondPointY);
                canvas.DrawLine(p,firstPoint, secondPoint);

                currX += 20;
            }
        }
    }
}
