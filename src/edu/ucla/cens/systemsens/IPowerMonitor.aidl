package edu.ucla.cens.systemsens;

import edu.ucla.cens.systemsens.IApplication;

interface IPowerMonitor
{

    /**
     * Register the application with power monitor
     *
     * @param   app     An implementation of IApplication
     */
    void register(IApplication app, int horizon);

    /**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IApplication
     */
    void unregister(IApplication app);

    /**
     * Set the battery deadline for the phone.
     * 
     * @param   deadline    deadline in minutes from now
     */
    void setDeadline(int deadline);


}
