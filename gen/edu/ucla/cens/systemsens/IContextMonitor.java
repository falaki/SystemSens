/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/falaki/temp/outsourced/cens-SystemSensPrivate-12529305141ad6d77cb5273409bca6fa2e767596/src/edu/ucla/cens/systemsens/IContextMonitor.aidl
 */
package edu.ucla.cens.systemsens;
public interface IContextMonitor extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.ucla.cens.systemsens.IContextMonitor
{
private static final java.lang.String DESCRIPTOR = "edu.ucla.cens.systemsens.IContextMonitor";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an edu.ucla.cens.systemsens.IContextMonitor interface,
 * generating a proxy if needed.
 */
public static edu.ucla.cens.systemsens.IContextMonitor asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.ucla.cens.systemsens.IContextMonitor))) {
return ((edu.ucla.cens.systemsens.IContextMonitor)iin);
}
return new edu.ucla.cens.systemsens.IContextMonitor.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_register:
{
data.enforceInterface(DESCRIPTOR);
edu.ucla.cens.systemsens.IContextReceiver _arg0;
_arg0 = edu.ucla.cens.systemsens.IContextReceiver.Stub.asInterface(data.readStrongBinder());
java.lang.String _arg1;
_arg1 = data.readString();
this.register(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_unregister:
{
data.enforceInterface(DESCRIPTOR);
edu.ucla.cens.systemsens.IContextReceiver _arg0;
_arg0 = edu.ucla.cens.systemsens.IContextReceiver.Stub.asInterface(data.readStrongBinder());
this.unregister(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.ucla.cens.systemsens.IContextMonitor
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Register the application with the context receiver (SystemSens)
     *
     * @param   app      An implementation of IContextReceiver
     * @param   name     ContextReceiver name
     */
public void register(edu.ucla.cens.systemsens.IContextReceiver app, java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((app!=null))?(app.asBinder()):(null)));
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_register, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IContextReceiver
     */
public void unregister(edu.ucla.cens.systemsens.IContextReceiver app) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((app!=null))?(app.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregister, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_register = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregister = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Register the application with the context receiver (SystemSens)
     *
     * @param   app      An implementation of IContextReceiver
     * @param   name     ContextReceiver name
     */
public void register(edu.ucla.cens.systemsens.IContextReceiver app, java.lang.String name) throws android.os.RemoteException;
/**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IContextReceiver
     */
public void unregister(edu.ucla.cens.systemsens.IContextReceiver app) throws android.os.RemoteException;
}
