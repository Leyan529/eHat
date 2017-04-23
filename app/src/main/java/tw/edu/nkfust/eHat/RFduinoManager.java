package tw.edu.nkfust.eHat;

public class RFduinoManager {
	final private static int STATE_RFDUINO_OUTOFRANGE = 0;
	final private static int STATE_STOPALL = 1;
	final private static int STATE_TIMER_ONE_SOUND = 2;
	final private static int STATE_TIMER_ONE_CLOSE = 3;
	final private static int STATE_TIMER_TWO_SOUND = 4;
	final private static int STATE_TIMER_TWO_CLOSE = 5;
	final private static int STATE_TIMER_THREE_SOUND = 6;
	final private static int STATE_TIMER_THREE_CLOSE = 7;
	final private static int STATE_TIMEPICKER_SOUND = 8;
	final private static int STATE_TIMEPICKER_CLOSE = 9;
	final private static int STATE_PHONE_RINGING = 10;
	final private static int STATE_PHONE_IDLE = 11;
	final private static int STATE_TURN_LEFT = 12;
	final private static int STATE_TURN_RIGHT = 13;
	final private static int STATE_GO_STRAIGHT = 14;
	final private static int STATE_TURN_SLIGHT_LEFT = 15;
	final private static int STATE_TURN_SLIGHT_RIGHT = 16;
	final private static int STATE_ARRIVE = 17;

	final private static int STATE_RATIO_20 = 30;
	final private static int STATE_RATIO_40 = 31;
	final private static int STATE_RATIO_60 = 32;
	final private static int STATE_RATIO_80 = 33;
	final private static int STATE_RATIO_100 = 34;

	private int state = STATE_STOPALL;
	private Thread stopAll, timesUp, timerStop, phoneRinging, phoneIdle, turnLeft, turnRight, goStraight, goLeft, goRight;
	private Thread ratio20, ratio40, ratio60, ratio80, ratio100;
	private boolean isContinue, isRinging;

	public boolean isOutOfRange() {
		if (state == STATE_RFDUINO_OUTOFRANGE) {
			return true;
		}// End of if-condition

		return false;
	}// End of isOutOfRange

	public void onStateChanged(int state) {
		this.state = state;
		updateState();
	}// End of stateChange

	public void updateState() {
		if (MainActivity.mRFduinoService != null) {
			if(MainActivity.mapMode.equals("normal")) {
				switch (state) {
					case STATE_RFDUINO_OUTOFRANGE:
						OutOfRange outOfRange = new OutOfRange();
						outOfRange.start();
						break;
					case STATE_STOPALL:
						stopAll = new StopAll();
						stopAll.start();
						break;
					case STATE_TIMER_ONE_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMER_ONE_CLOSE:
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_TIMER_TWO_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMER_TWO_CLOSE:
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_TIMER_THREE_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMER_THREE_CLOSE:
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_TIMEPICKER_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMEPICKER_CLOSE:
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_PHONE_RINGING:
						isRinging = true;
						phoneRinging = new PhoneRinging();
						phoneRinging.start();
						break;
					case STATE_PHONE_IDLE:
						isRinging = false;
						phoneIdle = new PhoneIdle();
						phoneIdle.start();
						break;
					case STATE_TURN_LEFT:
						turnLeft = new TurnLeft();
						turnLeft.start();
						break;
					case STATE_TURN_RIGHT:
						turnRight = new TurnRight();
						turnRight.start();
						break;
					case STATE_GO_STRAIGHT:
						goStraight = new GoStraight();
						goStraight.start();
						break;
					case STATE_TURN_SLIGHT_LEFT:
						goLeft = new GoLeft();
						goLeft.start();
						break;
					case STATE_TURN_SLIGHT_RIGHT:
						goRight = new GoRight();
						goRight.start();
						break;
					case STATE_ARRIVE:
						isArrive arrive = new isArrive();
						arrive.start();
						break;
				}// End of switch-condition
			} else if (MainActivity.mapMode.equals("sport")) {
				switch (state) {
					case STATE_RFDUINO_OUTOFRANGE:
						OutOfRange outOfRange = new OutOfRange();
						outOfRange.start();
						break;
					case STATE_STOPALL:
						stopAll = new StopAll(); 
						stopAll.start();
						break;
					case STATE_TIMER_ONE_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMER_ONE_CLOSE:
						isContinue = false;
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_TIMER_TWO_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMER_TWO_CLOSE:
						isContinue = false;
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_TIMER_THREE_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMER_THREE_CLOSE:
						isContinue = false;
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_TIMEPICKER_SOUND:
						if (!timesUp.isAlive()) {
							isContinue = true;
							timesUp = new TimesUp();
							timesUp.start();
						}// End of if-condition
						break;
					case STATE_TIMEPICKER_CLOSE:
						timerStop = new TimerStop();
						timerStop.start();
						break;
					case STATE_PHONE_RINGING:
						isRinging = true;
						phoneRinging = new PhoneRinging();
						phoneRinging.start();
						break;
					case STATE_PHONE_IDLE:
						isRinging = false;
						phoneIdle = new PhoneIdle();
						phoneIdle.start();
						break;
					case STATE_RATIO_20:
						ratio20 = new Ratio20();
						ratio20.start();
						break;
					case STATE_RATIO_40:
						ratio40 = new Ratio40();
						ratio40.start();
						break;
					case STATE_RATIO_60:
						ratio60 = new Ratio60();
						ratio60.start();
						break;
					case STATE_RATIO_80:
						ratio80 = new Ratio80();
						ratio80.start();
						break;
					case STATE_RATIO_100:
						ratio100 = new Ratio100();
						ratio100.start();
						break;
				}// End of switch-condition
			}// End of if-condition
		}// End of if-condition
	}// End of updateState

	class OutOfRange extends Thread {
		@Override
		public void run() {
			try {
				if(MainActivity.mapMode.equals("normal")) {
					for (int i = 0; i < 5; i++) {
						MainActivity.mRFduinoService.send(new byte[] { 3 });
						MainActivity.mRFduinoService.send(new byte[] { 4 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 30 });
						MainActivity.mRFduinoService.send(new byte[] { 40 });
						MainActivity.mRFduinoService.send(new byte[] { 2 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 20 });
						MainActivity.mRFduinoService.send(new byte[] { 5 });
						MainActivity.mRFduinoService.send(new byte[] { 6 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 50 });
						MainActivity.mRFduinoService.send(new byte[] { 60 });
						MainActivity.mRFduinoService.send(new byte[] { 2 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 20 });
					}// End of for-loop
				} else if (MainActivity.mapMode.equals("sport")) {
					for (int i = 0; i < 5; i++) {
						MainActivity.mRFduinoService.send(new byte[] { 2 });
						MainActivity.mRFduinoService.send(new byte[] { 6 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 3 });
						MainActivity.mRFduinoService.send(new byte[] { 5 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 4 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 20 });
						MainActivity.mRFduinoService.send(new byte[] { 60 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 30 });
						MainActivity.mRFduinoService.send(new byte[] { 50 });
						Thread.sleep(200);
						MainActivity.mRFduinoService.send(new byte[] { 40 });
					}// End of for-loop
				}// End of if-condition
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of OutOfRange

	class StopAll extends Thread {
		@Override
		public void run() {
			MainActivity.mRFduinoService.send(new byte[] { 0 });
		}// End of run
	}// End of StopAll

	class TimesUp extends Thread {
		@Override
		public void run() {
			while (isContinue) {
				try {
					if(MainActivity.mapMode.equals("normal")) {
						MainActivity.mRFduinoService.send(new byte[] { 2 });
						Thread.sleep(500);
						MainActivity.mRFduinoService.send(new byte[] { 20 });
						Thread.sleep(500);
					} else if (MainActivity.mapMode.equals("sport")) {
						MainActivity.mRFduinoService.send(new byte[] { 7 });
						Thread.sleep(500);
						MainActivity.mRFduinoService.send(new byte[] { 70 });
						Thread.sleep(500);
					}// End of if-condition
				} catch (InterruptedException e) {
					e.printStackTrace();
				}// End of try-catch;
			}// End of while-loop
		}// End of run
	}// End of TimesUp

	class TimerStop extends Thread {
		@Override
		public void run() {
			while (timesUp.isAlive());

			if(MainActivity.mapMode.equals("normal")) {
				MainActivity.mRFduinoService.send(new byte[] { 20 });
			} else if (MainActivity.mapMode.equals("sport")) {
				MainActivity.mRFduinoService.send(new byte[] { 70 });
			}// End of if-condition
		}// End of run
	}// End of TimerStop

	class PhoneRinging extends Thread {
		@Override
		public void run() {
			while (isRinging) {
				try {
					if(MainActivity.mapMode.equals("normal")) {
						MainActivity.mRFduinoService.send(new byte[] { 5 });
						MainActivity.mRFduinoService.send(new byte[] { 60 });
						Thread.sleep(300);
						MainActivity.mRFduinoService.send(new byte[] { 50 });
						MainActivity.mRFduinoService.send(new byte[] { 6 });
						Thread.sleep(300);
					} else if (MainActivity.mapMode.equals("sport")) {
						MainActivity.mRFduinoService.send(new byte[] { 7 });
						Thread.sleep(300);
						MainActivity.mRFduinoService.send(new byte[] { 70 });
						Thread.sleep(300);
					}// End of if-condition
				} catch (InterruptedException e) {
					e.printStackTrace();
				}// End of try-catch
			}// End of while-loop
		}// End of run
	}// End of PhoneRinging

	class PhoneIdle extends Thread {
		@Override
		public void run() {
			while (phoneRinging.isAlive());

			if(MainActivity.mapMode.equals("normal")) {
				MainActivity.mRFduinoService.send(new byte[] { 50 });
				MainActivity.mRFduinoService.send(new byte[] { 60 });
			} else if (MainActivity.mapMode.equals("sport")) {
				MainActivity.mRFduinoService.send(new byte[] { 70 });
			}// End of if-condition
		}// End of run
	}// End of PhoneIdle

	class TurnLeft extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					MainActivity.mRFduinoService.send(new byte[] { 3 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 30 });
					Thread.sleep(500);
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of TurnLeft

	class TurnRight extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					MainActivity.mRFduinoService.send(new byte[] { 4 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 40 });
					Thread.sleep(500);
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of TurnRight

	class GoStraight extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					MainActivity.mRFduinoService.send(new byte[] { 3 });
					MainActivity.mRFduinoService.send(new byte[] { 4 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 30 });
					MainActivity.mRFduinoService.send(new byte[] { 40 });
					Thread.sleep(500);
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of GoStraight

	class GoLeft extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					MainActivity.mRFduinoService.send(new byte[] { 3 });
					MainActivity.mRFduinoService.send(new byte[] { 5 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 30 });
					MainActivity.mRFduinoService.send(new byte[] { 50 });
					Thread.sleep(500);
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of GoLeft

	class GoRight extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 2; i++) {
					MainActivity.mRFduinoService.send(new byte[] { 4 });
					MainActivity.mRFduinoService.send(new byte[] { 6 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 40 });
					MainActivity.mRFduinoService.send(new byte[] { 60 });
					Thread.sleep(500);
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of GoRight

	class isArrive extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 5; i++) {
					MainActivity.mRFduinoService.send(new byte[] { 40 });
					MainActivity.mRFduinoService.send(new byte[] { 3 });
					Thread.sleep(300);
					MainActivity.mRFduinoService.send(new byte[] { 30 });
					MainActivity.mRFduinoService.send(new byte[] { 4 });
					Thread.sleep(300);
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of isArrive

	class Ratio20 extends Thread {
		@Override
		public void run() {
			try {
				for (int i = 0; i < 3; i++) {
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 2 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 20 });
				}// End of for-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch;
		}// End of run
	}// End of Ratio20

	class Ratio40 extends Thread {
		@Override
		public void run() {
			try {
				MainActivity.mRFduinoService.send(new byte[] { 2 });

				for (int i = 0; i < 3; i++) {
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 3 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 30 });
				}// End of for-loop

				MainActivity.mRFduinoService.send(new byte[] { 20 });
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch;
		}// End of run
	}// End of Ratio40

	class Ratio60 extends Thread {
		@Override
		public void run() {
			try {
				MainActivity.mRFduinoService.send(new byte[] { 2 });
				MainActivity.mRFduinoService.send(new byte[] { 3 });

				for (int i = 0; i < 3; i++) {
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 4 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 40 });
				}// End of for-loop

				MainActivity.mRFduinoService.send(new byte[] { 20 });
				MainActivity.mRFduinoService.send(new byte[] { 30 });
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch;
		}// End of run
	}// End of Ratio60

	class Ratio80 extends Thread {
		@Override
		public void run() {
			try {
				MainActivity.mRFduinoService.send(new byte[] { 2 });
				MainActivity.mRFduinoService.send(new byte[] { 3 });
				MainActivity.mRFduinoService.send(new byte[] { 4 });

				for (int i = 0; i < 3; i++) {
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 5 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 50 });
				}// End of for-loop

				MainActivity.mRFduinoService.send(new byte[] { 20 });
				MainActivity.mRFduinoService.send(new byte[] { 30 });
				MainActivity.mRFduinoService.send(new byte[] { 40 });
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch;
		}// End of run
	}// End of Ratio80

	class Ratio100 extends Thread {
		@Override
		public void run() {
			try {
				MainActivity.mRFduinoService.send(new byte[] { 2 });
				MainActivity.mRFduinoService.send(new byte[] { 3 });
				MainActivity.mRFduinoService.send(new byte[] { 4 });
				MainActivity.mRFduinoService.send(new byte[] { 5 });

				for (int i = 0; i < 3; i++) {
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 6 });
					Thread.sleep(500);
					MainActivity.mRFduinoService.send(new byte[] { 60 });
				}// End of for-loop

				MainActivity.mRFduinoService.send(new byte[] { 20 });
				MainActivity.mRFduinoService.send(new byte[] { 30 });
				MainActivity.mRFduinoService.send(new byte[] { 40 });
				MainActivity.mRFduinoService.send(new byte[] { 50 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 2 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 3 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 4 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 5 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 6 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 60 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 50 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 40 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 30 });
				Thread.sleep(300);
				MainActivity.mRFduinoService.send(new byte[] { 20 });
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch;
		}// End of run
	}// End of Ratio100
}// End of RFduinoManager
