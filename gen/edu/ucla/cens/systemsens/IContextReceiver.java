/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/falaki/temp/outsourced/cens-SystemSensPrivate-12529305141ad6d77cb5273409bca6fa2e767596/src/edu/ucla/cens/systemsens/IContextReceiver.aidl
 */
package edu.ucla.cens.systemsens;
public interface IContextReceiver extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.ucla.cens.systemsens.IContextReceiver
{
private static final java.lang.String DESCRIPTOR = "edu.ucla.cens.systemsens.IContextReceiver";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an edu.ucla.cens.systemsens.IContextReceiver interface,
 * generating a proxy if needed.
 */
public static edu.ucla.cens.systemsens.IContextReceiver asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.ucla.cens.systemsens.IContextReceiver))) {
return ((edu.ucla.cens.systemsens.IContextReceiver)iin);
}
return new edu.ucla.cens.systemsens.IContextReceiver.Stub.Proxy(obj);
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
case TRANSACTION_onReceive:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onReceive(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.ucla.cens.systemsens.IContextReceiver
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
     * Receives a record of context information.
     *
     * @param       record    String encoding of a JSONObject of the
     *                          record
     */
public void onReceive(java.lang.String record) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(record);
mRemote.transact(Stub.TRANSACTION_onReceive, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onReceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * Receives a record of context information.
     *
     * @param       record    String encoding of a JSONObject of the
     *                          record
     */
public void onReceive(java.lang.String record) throws android.os.RemoteException;
}
