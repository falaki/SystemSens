package edu.ucla.cens.systemsens;

import edu.ucla.cens.systemsens.IContextReceiver;

interface IContextMonitor
{

    /**
     * Register the application with the context receiver (SystemSens)
     *
     * @param   app      An implementation of IContextReceiver
     * @param   name     ContextReceiver name
     */
    void register(IContextReceiver app, String name);

    /**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IContextReceiver
     */
    void unregister(IContextReceiver app);


}
