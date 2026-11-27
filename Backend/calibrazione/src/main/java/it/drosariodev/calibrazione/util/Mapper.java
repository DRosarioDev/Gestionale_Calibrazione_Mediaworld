package it.drosariodev.calibrazione.util;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import it.drosariodev.calibrazione.enums.EMarca;
import it.drosariodev.calibrazione.enums.ETecnologia;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    private final static ModelMapper mapper = new ModelMapper();

    static {
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        // I DTO espongono gli ID come stringhe, il model li conserva come Long.
        mapper.addConverter(new AbstractConverter<Long, String>() {
            @Override
            protected String convert(Long source) {
                return source == null ? null : source.toString();
            }
        });

        // Marca e tecnologia sono enum nel model e stringhe leggibili nei DTO.
        mapper.addConverter(new AbstractConverter<EMarca, String>() {
            @Override
            protected String convert(EMarca source) {
                return etichettaMarca(source);
            }
        });

        mapper.addConverter(new AbstractConverter<String, EMarca>() {
            @Override
            protected EMarca convert(String source) {
                return marcaDaEtichetta(source);
            }
        });

        mapper.addConverter(new AbstractConverter<ETecnologia, String>() {
            @Override
            protected String convert(ETecnologia source) {
                return etichettaTecnologia(source);
            }
        });

        mapper.addConverter(new AbstractConverter<String, ETecnologia>() {
            @Override
            protected ETecnologia convert(String source) {
                return tecnologiaDaEtichetta(source);
            }
        });
    }

    public static void map(Object source, Object dest) {
        mapper.map(source, dest);
    }

    public static <D> D map(Object source, final Class<D> destType) {
        return mapper.map(source, destType);
    }

    public static <S, D> List<D> map(final List<S> source, final Class<D> destType) {
        List<D> dest = new ArrayList<D>();
        for (S element : source) {
            if (element == null) {
                continue;
            }
            dest.add(mapper.map(element, destType));
        }
        return dest;
    }

    private static String etichettaMarca(EMarca marca) {
        if (marca == null) {
            return null;
        }
        return switch (marca) {
            case SAMSUNG -> "Samsung";
            case LG -> "LG";
            case SONY -> "Sony";
            case PHILIPS -> "Philips";
            case PANASONIC -> "Panasonic";
            case TOSHIBA -> "Toshiba";
            case HISENSE -> "Hisense";
            case SHARP -> "Sharp";
            case VIZIO -> "Vizio";
            case TCL -> "TCL";
            case ALTRO -> "Altro";
            default -> marca.name();
        };
    }

    private static String etichettaTecnologia(ETecnologia tecnologia) {
        if (tecnologia == null) {
            return null;
        }
        return switch (tecnologia) {
            case UHD -> "UHD";
            case OLED -> "OLED";
            case QLED -> "QLED";
            case NEO_QLED -> "Neo QLED";
            case MINI_LED -> "Mini LED";
            case LED -> "LED";
            case MICRO_LED -> "Micro LED";
            case LCD -> "LCD";
            case PLASMA -> "Plasma";
            case ALTRO -> "Altro";
            default -> tecnologia.name();
        };
    }

    private static EMarca marcaDaEtichetta(String valore) {
        String normalizzato = normalizza(valore);
        if (normalizzato.isEmpty()) {
            return null;
        }
        for (EMarca marca : EMarca.values()) {
            if (normalizza(marca.name()).equals(normalizzato)
                    || normalizza(etichettaMarca(marca)).equals(normalizzato)) {
                return marca;
            }
        }
        return null;
    }

    private static ETecnologia tecnologiaDaEtichetta(String valore) {
        String normalizzato = normalizza(valore);
        if (normalizzato.isEmpty()) {
            return null;
        }
        for (ETecnologia tecnologia : ETecnologia.values()) {
            if (normalizza(tecnologia.name()).equals(normalizzato)
                    || normalizza(etichettaTecnologia(tecnologia)).equals(normalizzato)) {
                return tecnologia;
            }
        }
        return null;
    }

    private static String normalizza(String valore) {
        return valore == null ? "" : valore.trim().replaceAll("[\\s_-]+", "").toUpperCase();
    }
}
