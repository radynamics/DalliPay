package com.radynamics.dallipay.transformation.bip21;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BitcoinPaymentURI {

    private static final String SCHEME = "bitcoin:";
    private static final String PARAMETER_AMOUNT = "amount";
    private static final String PARAMETER_LABEL = "label";
    private static final String PARAMETER_MESSAGE = "message";

    private final String address;
    private final HashMap<String, Parameter> parameters = new HashMap<>();

    private BitcoinPaymentURI(Builder builder) {
        this.address = builder.address;

        if (builder.amount != null) {
            parameters.put(PARAMETER_AMOUNT, new Parameter(String.valueOf(builder.amount), false));
        }

        if (builder.label != null) {
            parameters.put(PARAMETER_LABEL, new Parameter(builder.label, false));
        }

        if (builder.message != null) {
            parameters.put(PARAMETER_MESSAGE, new Parameter(builder.message, false));
        }

        if (builder.otherParameters != null) {
            parameters.putAll(builder.otherParameters);
        }
    }

    public String getAddress() {
        return address;
    }

    public Double getAmount() {
        if (parameters.get(PARAMETER_AMOUNT) == null) {
            return null;
        }
        return Double.valueOf(parameters.get(PARAMETER_AMOUNT).getValue());
    }

    public String getLabel() {
        if (parameters.get(PARAMETER_LABEL) == null) {
            return null;
        }
        return parameters.get(PARAMETER_LABEL).getValue();
    }

    public String getMessage() {
        if (parameters.get(PARAMETER_MESSAGE) == null) {
            return null;
        }
        return parameters.get(PARAMETER_MESSAGE).getValue();
    }

    public HashMap<String, Parameter> getParameters() {
        var filteredParameters = new HashMap<>(parameters);
        filteredParameters.remove(PARAMETER_AMOUNT);
        filteredParameters.remove(PARAMETER_LABEL);
        filteredParameters.remove(PARAMETER_MESSAGE);
        return filteredParameters;
    }

    public String getURI() {
        String queryParameters = null;
        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            if (queryParameters == null) {
                if (entry.getValue().isRequired()) {
                    queryParameters = String.format("req-%s=%s", URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8).replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), StandardCharsets.UTF_8).replace("+", "%20"));
                    continue;
                }

                queryParameters = String.format("%s=%s", URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8).replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), StandardCharsets.UTF_8).replace("+", "%20"));
                continue;
            }

            if (entry.getValue().isRequired()) {
                queryParameters = String.format("%s&req-%s=%s", queryParameters, URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8).replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), StandardCharsets.UTF_8).replace("+", "%20"));
                continue;
            }

            queryParameters = String.format("%s&%s=%s", queryParameters, URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8).replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), StandardCharsets.UTF_8).replace("+", "%20"));
        }

        return String.format("%s%s%s", SCHEME, getAddress(), queryParameters == null ? "" : String.format("?%s", queryParameters));
    }

    public static BitcoinPaymentURI parse(String string) {
        string = URLDecoder.decode(string, StandardCharsets.UTF_8);

        if (string == null) {
            return null;
        }

        if (string.isEmpty()) {
            return null;
        }

        if (!string.toLowerCase().startsWith(SCHEME)) {
            return null;
        }

        var bitcoinPaymentURIWithoutScheme = string.replaceFirst(SCHEME, "");
        var bitcoinPaymentURIElements = new ArrayList<>(Arrays.asList(bitcoinPaymentURIWithoutScheme.split("\\?")));

        if (bitcoinPaymentURIElements.size() != 1 && bitcoinPaymentURIElements.size() != 2) {
            return null;
        }

        if (bitcoinPaymentURIElements.get(0).length() == 0) {
            return null;
        }

        if (bitcoinPaymentURIElements.size() == 1) {
            return new Builder().address(bitcoinPaymentURIElements.get(0)).build();
        }

        var queryParametersList = Arrays.asList(bitcoinPaymentURIElements.get(1).split("&"));

        if (queryParametersList.isEmpty()) {
            return new Builder().address(bitcoinPaymentURIElements.get(0)).build();
        }

        var queryParametersFiltered = new HashMap<String, String>();
        for (String query : queryParametersList) {
            var queryParameter = query.split("=");

            try {
                queryParametersFiltered.put(queryParameter[0], queryParameter[1]);
            } catch (ArrayIndexOutOfBoundsException exception) {
                exception.printStackTrace();
                return null;
            }
        }

        var bitcoinPaymentURIBuilder = new Builder().address(bitcoinPaymentURIElements.get(0));

        if (queryParametersFiltered.containsKey(PARAMETER_AMOUNT)) {
            bitcoinPaymentURIBuilder.amount(Double.valueOf(queryParametersFiltered.get(PARAMETER_AMOUNT)));
            queryParametersFiltered.remove(PARAMETER_AMOUNT);
        }

        if (queryParametersFiltered.containsKey(PARAMETER_LABEL)) {
            bitcoinPaymentURIBuilder.label(queryParametersFiltered.get(PARAMETER_LABEL));
            queryParametersFiltered.remove(PARAMETER_LABEL);
        }

        if (queryParametersFiltered.containsKey(PARAMETER_MESSAGE)) {
            bitcoinPaymentURIBuilder.message(queryParametersFiltered.get(PARAMETER_MESSAGE));
            queryParametersFiltered.remove(PARAMETER_MESSAGE);
        }

        for (Map.Entry<String, String> entry : queryParametersFiltered.entrySet()) {
            bitcoinPaymentURIBuilder.parameter(entry.getKey(), entry.getValue());
        }

        return bitcoinPaymentURIBuilder.build();
    }

    public static class Builder {
        private String address;
        private Double amount;
        private String label;
        private String message;
        private HashMap<String, Parameter> otherParameters;

        public Builder() {
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder parameter(String key, String value) {
            if (otherParameters == null) {
                otherParameters = new HashMap<String, Parameter>();
            }

            if (key.startsWith("req-")) {
                otherParameters.put(key.replace("req-", ""), new Parameter(value, true));
                return this;
            }

            otherParameters.put(key, new Parameter(value, false));
            return this;
        }

        public Builder requiredParameter(String key, String value) {
            if (otherParameters == null) {
                otherParameters = new HashMap<String, Parameter>();
            }

            otherParameters.put(key, new Parameter(value, true));
            return this;
        }

        public BitcoinPaymentURI build() {
            return new BitcoinPaymentURI(this);
        }
    }
}
