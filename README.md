# CryptoIso20022 Interop

This project as a prove of concept (PoC) enables interoperability between [ISO 20022](https://www.iso20022.org) file formats and crypto ledger payments. It aims to faciliate sending and processing received crypto payments within existing financial software's ISO 20022 capabilities.

## Supported crypto ledgers
- [XRPL](https://xrpl.org/)

## Supported ISO 20022 formats
### pain.001 (sending payments)
Given payment instructions in pain.001 format can be transformed into crypto payments. The resulting crypto payments can directly be sent over the used crypto ledger. Additional [TransformationInstruction](#TransformationInstruction) contains further information for transformation. PoC limited to a subset of [Swiss implementation guidelines for payments SPS 2021 v1.1](https://www.six-group.com/de/products-services/banking-services/standardization/iso-payments.html)

### camt.053 (received payments)
Given a crypto wallet address transactions from ledger are fetched and transformed into cash management information. Produces a camt.053 format. PoC limited to a subset of [Swiss implementation guidelines for cash management SPS 2021 v1.7.2](https://www.six-group.com/de/products-services/banking-services/standardization/iso-payments.html)

## TransformationInstruction
Contains additional information needed for transformation such as account mapping (IBAN) to crypto ledger wallet address and exchange rate providers. It's defined as JSON in transforminstruction.json.

## CryptoIso20022Interop.jar console parameters
Examples are shown in run_pain001.cmd and run_camt054.cmd.
```
-a              Action to perform
                "pain001ToCrypto" to transform given pain.001 file into crypto payments
                "cryptoToCamt054" to fetch received payments of a given wallet and save as camt.054 file
-in             Input pain.001 file for "pain001ToCrypto"
-out            Output camt.054 file for "cryptoToCamt054"
-ti             File containing TransformationInstruction
-wallet         Wallet used as sender for "pain001ToCrypto" or as source for "cryptoToCamt054"
-walletSecret   Secret/PrivateKey of sending wallet. Only needed for "pain001ToCrypto".
-n              "test" for Testnet, "live" for Livenet
-from           Transactions from in format yyyyMMddHHmmss (last 7 days if omitted)
-until          Transactions until in format yyyyMMddHHmmss (now if omitted)
```

## Run demo
1. Create two wallets on XRPL Testnet [xrpl.org Faucets](https://www.xrpl.org/xrp-testnet-faucet.html)
2. Choose first wallet as "sender" and second one as "receiver"
3. Edit run_pain001.cmd and enter "sender" -wallet and -walletSecret (for sending transaction)
4. Edit run_camt054.cmd and enter "receiver" -wallet
5. Edit transforminstruction.json
    * 5.1 Replace "rB4dHhAmYNzGEFrZxW2Djq8BabPdN9HnEV" with your "sender" wallet
    * 5.2 Replace "rBUBKY6RU1PGwXvFddhPuDbjoqwGx4N3qH" with your "receiver" wallet


