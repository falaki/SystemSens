package edu.ucla.cens.systemsens.util;

import java.text.DecimalFormat;

import edu.ucla.cens.systemlog.Log;

public class CircularQueue
{
    private final static String TAG = "CircularQueue";

    private double[] data;
    private double mLastValue;
    private int mSize;
    private int mHead;
    private DecimalFormat mDF;

    public CircularQueue(int size)
    {
        mSize = size;
        mHead = 0;
        mLastValue = 0.0;
        data = new double[mSize];

        for (int i = 0; i < mSize; i++)
            data[i] = 0.0;

        mDF = new DecimalFormat();
        mDF.setMaximumFractionDigits(3);


        Log.i(TAG, "New CircularBuffer with size: " + mSize);

    }

    /**
     * Adds the difference between the given value and the previously
     * added value to the circular queue. 
     *
     * @param       value           new value
     */
    public void add(double value)
    {
        //Log.i(TAG, "Received " + value + " to add");

        if (mLastValue == 0.0)
        {
            Log.i(TAG, "Received first value " + value );
            mLastValue = value;
            return;
        }

        double curValue;
        mHead = (mHead + 1) % mSize;

        curValue = value - mLastValue;
        data[mHead] = curValue;
        mLastValue = value;

        Log.i(TAG, "Received " + mDF.format(value)
                + ", Inserted " + mDF.format(curValue)
                + ", current sum: " + mDF.format(getSum()));

    }


    /**
     * Returns the sum of all the values in the queue.
     *
     * @return          current sum of the queue
     */
    public double getSum()
    {
        double sum = 0.0;

        for (int i = 0; i < mSize; i++)
            sum += data[i];

        return sum;

    }

    /**
     * Returns the size of the queue.
     *
     * @return          size of the queue
     */
    public int getSize()
    {
        return mSize;
    }

}
