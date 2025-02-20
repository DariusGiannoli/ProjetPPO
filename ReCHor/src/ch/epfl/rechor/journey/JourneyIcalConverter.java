package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

public class JourneyIcalConverter {

    private JourneyIcalConverter() {
    }

    public String toIcalendar(Journey journey){

        IcalBuilder builder = new IcalBuilder();

        StringJoiner joiner = new StringJoiner("\n");


        for(int i = 0; i < journey.legs().size(); i++) {
            switch (journey.legs().get(i)) {
                case Journey.Leg.Foot f -> joiner.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> joiner.add(FormatterFr.formatLeg(t));
            }
        }


        builder.begin(IcalBuilder.Component.VCALENDAR).add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor").begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now())
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime())
                .add(IcalBuilder.Name.SUMMARY, journey.depStop().toString() + " → " + journey.arrStop().toString())
                .add(IcalBuilder.Name.DESCRIPTION, joiner.toString())
                .end().end();



        return builder.toString();
    }
}
