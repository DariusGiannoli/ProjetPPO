package ch.epfl.rechor;


import java.security.cert.CRL;
import java.time.LocalDateTime;
import java.util.ArrayList;

public final class IcalBuilder {

    public enum Component {
        VCALENDAR,
        VEVENT;
    }

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION;
    }

    private String CRLF = "\r\n";

    private ArrayList<Component> begunComponents = new ArrayList<>();

    private StringBuilder ICalString = new StringBuilder();

    private String textAdd(String name, String str2) {
        StringBuilder sb = new StringBuilder();

        String strTotal = name + ":" + str2;

        if (strTotal.length() <= 75) {
            sb.append(name).append(":").append(str2).append(CRLF);
        } else {
            int i = 0;
            while (strTotal.length() > 74) {
                if (i == 0) {
                    sb.append(strTotal.substring(0, 75)).append(CRLF);
                    strTotal = strTotal.substring(75);

                } else {
                    sb.append(" ").append(strTotal.substring(0, 74)).append(CRLF);
                    strTotal = strTotal.substring(74);
                }
                i++;
            }
            sb.append(" ").append(strTotal).append(CRLF);
        }
        return sb.toString();

    }

    public IcalBuilder add(Name name, String value) {
        String nameString = name.toString();
        ICalString.append(textAdd(nameString, value));

        return this;
    }

    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        String dateTimeString = dateTime.toString();
        String nameString = name.toString();
        ICalString.append(textAdd(nameString, dateTimeString));

        return this;
    }

    public IcalBuilder begin(Component component) {
        String componentString = component.toString();
        ICalString.append("BEGIN:").append(componentString).append(CRLF);
        begunComponents.add(component);
        return this;
    }

    public IcalBuilder end(){
        Preconditions.checkArgument(!begunComponents.isEmpty());
        int listSize = begunComponents.size();
        ICalString.append("END:").append((begunComponents.get(listSize-1)).toString()).append(CRLF);
        begunComponents.remove(listSize - 1);

        return this;
    }

    public String build(){
        Preconditions.checkArgument(begunComponents.isEmpty());

        return ICalString.toString();
    }


}
