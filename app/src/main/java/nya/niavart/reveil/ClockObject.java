package nya.niavart.reveil;

import android.os.Parcel;
import android.os.Parcelable;

public class ClockObject implements Parcelable {

    String time;
    Integer snooze1 = 1;
    Integer snooze2 = 1;
    Integer snooze3 = 1;
    Integer inc = 0;

    public ClockObject(String time) {

        this.time = time;
    }

    public ClockObject(ClockObject clone) {

        this.time = clone.time;
        this.snooze3 = clone.snooze3;
        this.snooze2 = clone.snooze2;
        this.snooze1 = clone.snooze1;
        this.inc = clone.inc;
    }


    public ClockObject(String time, Integer s1, Integer s2, Integer s3) {

        this.time = time;
        this.snooze3 = s3;
        this.snooze2 = s2;
        this.snooze1 = s1;
    }

    public String getTime() {
        return time;
    }

    public Integer getSnooze() {
        if (inc == 1)
            return snooze1;
        else if (inc == 2)
            return snooze2;
        else
            return snooze3;
    }

    public void setSnooze(Integer c, Integer val) {
        if (c == 1)
            snooze1 = val;
        else if (c == 2)
            snooze2 = val;
        else
            snooze3 = val;
    }

    public Integer getInc() {
        return inc;
    }

    public Integer getSnooze1() {
        return snooze1;
    }

    public Integer getSnooze2() {
        return snooze2;
    }

    public Integer getSnooze3() {
        return snooze3;
    }

    public void Inc() {
        inc += 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(time);
        out.writeInt(snooze1);
        out.writeInt(snooze2);
        out.writeInt(snooze3);
        out.writeInt(inc);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<ClockObject> CREATOR = new Parcelable.Creator<ClockObject>() {
        public ClockObject createFromParcel(Parcel in) {
            return new ClockObject(in);
        }

        public ClockObject[] newArray(int size) {
            return new ClockObject[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private ClockObject(Parcel in) {
        time = in.readString();
        snooze1 = in.readInt();
        snooze2 = in.readInt();
        snooze3 = in.readInt();
        inc = in.readInt();
    }
}
