package edu.ucla.cens.systemsens;


interface IAdaptiveApplication
{

    /**
     * Returns the name of this application. 
     * E.g. WiFiGPSLocation
     *
     * @return      name of the application
     */
    String getName();

    /**
     * Returns the list of names of work units.
     * E.g. <"GPS", "WiFiScan">
     *
     * @return      names of work units.
     */
    List identifyList();



    /**
     * Return a vector of doubles indicating the amount of work done.
     * Each adaptive application should count its its major energy
     * consuming operations. Each time this method is called the
     * cumulative count of all the activities should be returned. 
     * The order should be consistant. The counts can be fractional 
     * values (double).
     * E.g. <126.0, 1763.0>
     *
     * @return      List of doubles indicating work units
     *
     */
    List getWork();

    /**
     * Sets the amount of work that the application is allowed
     * to perform. The order of this list is the same as that
     * retuend by getWork().
     *
     * @param       workLimit    List of allowed work units
     */
    void setWorkLimit(in List workLimit);


    /**
     * Sets the allowed rate of work. Each element of the list
     * indicates the number of work units that can be done during
     * each second.
     *
     * @param       workRate    List of allowed rates
    void setWorkRate(in List workRate);
     */

}
