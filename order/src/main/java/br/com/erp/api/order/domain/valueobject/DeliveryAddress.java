package br.com.erp.api.order.domain.valueobject;

import br.com.erp.api.order.domain.exception.InvalidDeliveryAddressException;

/**
 * Endereço de entrega do pedido. A loja entrega exclusivamente em
 * Pirenópolis - GO, por isso {@code city} e {@code state} são fixos no
 * domínio e o CEP precisa pertencer à faixa local (72980-000 a 72989-999).
 */
public final class DeliveryAddress {

    public static final String CITY     = "Pirenópolis";
    public static final String STATE    = "GO";
    private static final int CEP_MIN    = 72_980_000;
    private static final int CEP_MAX    = 72_989_999;

    private final String cep;
    private final String street;
    private final String number;
    private final String complement;
    private final String neighborhood;
    private final String city;
    private final String state;

    private DeliveryAddress(String cep, String street, String number, String complement,
                            String neighborhood, String city, String state) {
        this.cep          = cep;
        this.street       = street;
        this.number       = number;
        this.complement   = complement;
        this.neighborhood = neighborhood;
        this.city         = city;
        this.state        = state;
    }

    /**
     * Cria um endereço novo aplicando as invariantes de negócio: CEP da faixa
     * local e cidade/estado fixos (não aceitos como input do cliente).
     */
    public static DeliveryAddress create(String cep, String street, String number,
                                         String complement, String neighborhood) {
        if (!isCepInLocalRange(cep)) {
            throw new InvalidDeliveryAddressException(
                    "CEP inválido: a loja entrega apenas em %s - %s (CEP deve estar entre 72980-000 e 72989-999)"
                            .formatted(CITY, STATE));
        }
        return new DeliveryAddress(cep, street, number, complement, neighborhood, CITY, STATE);
    }

    /**
     * Valida se o CEP pertence à faixa local de entrega (72980-000 a 72989-999),
     * ignorando formatação (hífen/espaços) e exigindo exatamente 8 dígitos.
     */
    private static boolean isCepInLocalRange(String cep) {
        if (cep == null) {
            return false;
        }
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() != 8) {
            return false;
        }
        int value = Integer.parseInt(digits);
        return value >= CEP_MIN && value <= CEP_MAX;
    }

    /**
     * Reconstrói a partir de dados já persistidos, sem reaplicar as invariantes
     * de criação (a fonte de verdade é o banco).
     */
    public static DeliveryAddress restore(String cep, String street, String number,
                                          String complement, String neighborhood,
                                          String city, String state) {
        return new DeliveryAddress(cep, street, number, complement, neighborhood, city, state);
    }

    public String getCep()          { return cep; }
    public String getStreet()       { return street; }
    public String getNumber()       { return number; }
    public String getComplement()   { return complement; }
    public String getNeighborhood() { return neighborhood; }
    public String getCity()         { return city; }
    public String getState()        { return state; }
}
