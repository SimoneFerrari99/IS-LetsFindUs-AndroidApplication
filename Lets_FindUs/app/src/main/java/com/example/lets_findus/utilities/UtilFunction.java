package com.example.lets_findus.utilities;

import org.joda.time.DateTimeComparator;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UtilFunction {

    public static void filterItems(List<MeetingPerson> meetingsToFilter, List<MeetingPerson> allMeetings, Map<String, String> filterOptions) {
        meetingsToFilter.clear();
        meetingsToFilter.addAll(allMeetings);
        Iterator<MeetingPerson> mpIterator = meetingsToFilter.iterator();
        while (mpIterator.hasNext()) {
            MeetingPerson elem = mpIterator.next();
            switch (filterOptions.get("sex")) {
                case "Maschio":
                    if (elem.person.sex != Person.Sex.MALE)
                        mpIterator.remove();
                    break;
                case "Femmina":
                    if (elem.person.sex != Person.Sex.FEMALE)
                        mpIterator.remove();
                    break;
                case "Altro":
                    if (elem.person.sex != Person.Sex.OTHER)
                        mpIterator.remove();
                    break;
            }
            switch (filterOptions.get("date")) {
                case "Oggi":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 0)) != 0)
                        mpIterator.remove();
                    break;
                case "Ieri":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 1)) != 0)
                        mpIterator.remove();
                    break;
                case "2 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 2)) != 0)
                        mpIterator.remove();
                    break;
                case "3 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 3)) != 0)
                        mpIterator.remove();
                    break;
                case "4 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 4)) != 0)
                        mpIterator.remove();
                    break;
                case "5 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 5)) != 0)
                        mpIterator.remove();
                    break;
                case "6 giorni fa":
                    if (DateTimeComparator.getDateOnlyInstance().compare(elem.meeting.date, DateWithArithmetic.subtractDays(Calendar.getInstance().getTime(), 6)) != 0)
                        mpIterator.remove();
                    break;
            }
            switch (filterOptions.get("hour")) {
                case "00.00 – 04.00":
                    if (!DateWithArithmetic.isInHourRange(elem.meeting.date, 0, 4))
                        mpIterator.remove();
                    break;
                case "04.00 – 08.00":
                    if (!DateWithArithmetic.isInHourRange(elem.meeting.date, 4, 8))
                        mpIterator.remove();
                    break;
                case "08.00 – 12.00":
                    if (!DateWithArithmetic.isInHourRange(elem.meeting.date, 8, 12))
                        mpIterator.remove();
                    break;
                case "12.00 – 16.00":
                    if (!DateWithArithmetic.isInHourRange(elem.meeting.date, 12, 16))
                        mpIterator.remove();
                    break;
                case "16.00 – 20.00":
                    if (!DateWithArithmetic.isInHourRange(elem.meeting.date, 16, 20))
                        mpIterator.remove();
                    break;
                case "20.00 – 24.00":
                    if (!DateWithArithmetic.isInHourRange(elem.meeting.date, 20, 24))
                        mpIterator.remove();
                    break;
            }
        }
    }
}
