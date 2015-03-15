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
            SerialPort sp = (SerialPort)sender;
            string data = sp.ReadExisting();

            string[] splitData = data.Split(',');

            for (int i = 0; i < splitData.Length; i++ )
            {
                if (splitData[i] != "")
                {
                    testingList.Add(Convert.ToInt32(splitData[i]));
                    dataManager.addData(Convert.ToInt32(splitData[i]));
                }
            }

                
        }

        private void button1_Click(object sender, EventArgs e)
        {
            listBox1.Items.Clear();
            listBox1.Items.Add("Port Open");
            serialPort1.PortName = "COM5";
            serialPort1.Open();
            timer1.Enabled = true;
        }

        private void button2_Click(object sender, EventArgs e)
        {
            serialPort1.Close();

            listBox1.Items.Add("Port closed");
            timer1.Enabled = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
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

                //Do display things
                int[] dataToDisplay = dataManager.getArray();
                displayManager.Draw(dataToDisplay);
                mainCanvas.DrawImage(offScreenBitmap, 0, 0);
                mainCanvas.DrawLine(new Pen(Color.Black), new Point(400, 400), new Point(400, 50));
                mainCanvas.DrawLine(new Pen(Color.Black), new Point(400, 400), new Point(1000, 400));
            }

            previousListLength = testingList.Count();
        }
    }
}
