using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace SensorDisplay
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        List<int> testingList;
        int previousListLength;
        int countSinceLastChange;
        Graphics mainCanvas;
        Graphics offScreenCanvas;
        Bitmap offScreenBitmap;
        InputDataManager dataManager;
        DistanceDisplayManager displayManager;

        private void Form1_Load(object sender, EventArgs e)
        {
            testingList = new List<int>();
            previousListLength = 0;
            countSinceLastChange = 0;

            mainCanvas = CreateGraphics();
            offScreenBitmap = new Bitmap(this.Width, this.Height);
            offScreenCanvas = Graphics.FromImage(offScreenBitmap);
            displayManager = new DistanceDisplayManager(offScreenCanvas);
            dataManager = new InputDataManager(30);
        }

        private void serialPort1_DataReceived(object sender, System.IO.Ports.SerialDataReceivedEventArgs e)
        {
            //Get the data being received in serial port
            SerialPort sp = (SerialPort)sender;
            string data = sp.ReadExisting();

            //Catch exception error when "/r/n" comes through
            try
            {
                //Add data into appropriate places
                testingList.Add(Convert.ToInt32(data));
                dataManager.addData(Convert.ToInt32(data));
            }
            catch (FormatException E)
            {
                //Write error to console
                Console.WriteLine(E.ToString());
            }

                
        }

        //Button to start everything up
        private void button1_Click(object sender, EventArgs e)
        {
            listBox1.Items.Clear();
            listBox1.Items.Add("Port Open");

            //Start reading data from serial port
            serialPort1.PortName = "COM5";
            serialPort1.Open();
            timer1.Enabled = true;
        }

        //Button to close everything off
        private void button2_Click(object sender, EventArgs e)
        {
            serialPort1.Close();

            listBox1.Items.Add("Port closed");
            timer1.Enabled = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            //Only do things if there has been new data received from serial port
            if (previousListLength != testingList.Count())
            {
                //Display collected data into listbox
                listBox1.Items.Clear();
                listBox1.Items.Add("Port Open");
                for (int i = 0; i < testingList.Count(); i++)
                {
                    listBox1.Items.Add(testingList[i]);
                }
                listBox1.TopIndex = listBox1.Items.Count - 1;

                //Do graphical display things
                int[] dataToDisplay = dataManager.getArray();
                displayManager.Draw(dataToDisplay);
                offScreenCanvas.DrawLine(new Pen(Color.Black), new Point(400, 400), new Point(400, 50));
                offScreenCanvas.DrawLine(new Pen(Color.Black), new Point(400, 400), new Point(1000, 400));
                mainCanvas.DrawImage(offScreenBitmap, 0, 0);
            }

            previousListLength = testingList.Count();
        }
    }
}
