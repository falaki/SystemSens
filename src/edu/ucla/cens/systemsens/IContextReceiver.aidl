package edu.ucla.cens.systemsens;


interface IContextReceiver
{

    /**
     * Receives a record of context information.
     *
     * @param       record    String encoding of a JSONObject of the
     *                          record
     */
    void onReceive(in String record);

}
