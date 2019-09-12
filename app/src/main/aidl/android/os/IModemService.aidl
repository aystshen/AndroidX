package android.os;
 
/** {@hide} */
interface IModemService
{
	int powerOn();
	int powerOff();
	int reset();
	int wakeup();
	int sleep();
	boolean isWakeup();
	boolean isPowerOn();
}
