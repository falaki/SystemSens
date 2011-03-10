package edu.ucla.cens.systemsens;

import edu.ucla.cens.systemsens.IAdaptiveApplication;

interface IPowerMonitor
{

    /**
     * Register the application with power monitor
     *
     * @param   app     An implementation of IAdaptiveApplication
     */
    void register(IAdaptiveApplication app, int horizon);

    /**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IAdaptiveApplication
     */
    void unregister(IAdaptiveApplication app);

    /**
     * Set the battery deadline for the phone.
     * 
     * @param   deadline    deadline in minutes from now
     */
    void setDeadline(int deadline);


}
