package com.jyotimoykashyap.bookmyshowdesign.locker;

public record ShowSeatKey(int showId, int seatId) implements Comparable<ShowSeatKey> {

    @Override
    public int compareTo(ShowSeatKey o) {
        int showCmp = Integer.compare(this.showId, o.showId);

        return showCmp != 0 ? showCmp : Integer.compare(this.seatId, o.seatId);
    }

}
