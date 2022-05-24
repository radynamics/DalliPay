# CryptoIso20022 Interop

This project enables interoperability between [ISO 20022](https://www.iso20022.org) file formats and cryptocurrency payments. It aims to facilitate sending and processing received crypto payments within existing financial software's ISO 20022 capabilities.

As a ***payer*** you can import pain.001 xml files and transform payment instructions into cryptocurrency payments. For every transaction you like to send using cryptocurrency you must define a cryptocurrency address (receiver wallet) for a given iban or proprietary account number.

As a ***payee*** you can fetch received cryptocurrency payments from your wallet and transform received payments into camt.054 xml format.

Latest releases for various platforms are available in [releases](https://github.com/rsteimen/CryptoIso20022Interop/releases) section.

#### Demo (YouTube)
[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/-u307nu72SQ/0.jpg)](https://www.youtube.com/watch?v=-u307nu72SQ)

#### Supported crypto ledgers (cryptocurrencies)
- [XRPL](https://xrpl.org/) (XRP)

#### Supported ISO 20022 formats
Following *pain.001 (sending payments)* format versions are tested. Other versions may also work.
- pain.001.001.03
- pain.001.001.09

Following *camt.054 (received payments)* format versions are available for export.
- camt.054.001.02
- camt.054.001.04
- camt.054.001.09

## Persisted data
Your private key (wallet secret) is not persisted and only used while sending payments. Configuration and settings are stored in user.home/CryptoIso20022Interop or %APPDATA%\CryptoIso20022Interop on windows.

We are ***not able to restore your password*** for persisted data. Your persisted data will be lost.  
***Never send*** us or anyone else your private key (wallet secret). Doing so will allow the receiver to transfer all your funds.

## CryptoIso20022Interop console parameters
For better integration into business processes following console parameters are available.
#### Sending payments (pain.001)
```
-a              pain001ToCrypto         Action to perform
-in             <file path>             Input pain.001 xml file containing payment instructions
-wallet         <wallet address>        Wallet used as sender
-n              test, live              Testnet or Livenet

Examples:
- java -jar CryptoIso20022Interop.jar -a pain001ToCrypto -in C:\demo\UBS_20210603_00001.xml -n test
- CryptoIso20022Interop.exe -a pain001ToCrypto -in C:\demo\UBS_20210603_00001.xml -n test
```
#### Receiving payments (camt.054)
```
-a              cryptoToCamt054         Action to perform
-out            <file path>             Output camt.054 xml file
-wallet         <wallet address>        Wallet used as source
-n              test, live              Testnet or Livenet
-from           20220521101200          Transactions from in format yyyyMMddHHmmss (last 7 days if omitted)
-until          20220509070800          Transactions until in format yyyyMMddHHmmss (now if omitted)

Examples:
- java -jar CryptoIso20022Interop.jar -a cryptoToCamt054 -out C:\temp\test.xml -wallet rPdvC6ccq8hCdPKSPJkPmyZ4Mi1oG2FFkT
- CryptoIso20022Interop.exe -a cryptoToCamt054 -out C:\temp\test.xml -wallet rPdvC6ccq8hCdPKSPJkPmyZ4Mi1oG2FFkT
```
## Run demo on Testnet
#### Sending payments (pain.001)
1. Create wallet with secret on XRPL Testnet [xrpl.org Faucets](https://www.xrpl.org/xrp-testnet-faucet.html)
2. Click globe icon on top right of the window to switch to Testnet
3. On tab \'Send\' select an ISO 20022 pain.001 XML file as source
4. Click into cells \'Sender Wallet\' and \'Receiver Wallet\' to define sender and receivers
    - \'Sender Wallet\' is your own wallet where payments are sent from 
    - \'Receiver Wallet\' is the wallet address of shown \'Receiver Account\'. Ask your creditor for his wallet address.
5. After solving possible issues, press \'Send Payments\'
6. After sending, green checkmarks per row indicate success
    - Click \'detail...\' for more information
    - In detail window click \'show ledger transaction...\' near \'Booked\' to open transaction in ledger explorer


#### Receiving payments (camt.054)
1. On tab \'Receive\' enter a receiver wallet
    - Click globe icon on top right of the window to switch between Livenet and Testnet
2. Adjust filter if needed and press \'Refresh\'
    - On Testnet maybe only recent transactions are available
3. Click into cells \'Sender Account\' and \'Receiver Account\' to define sender and receivers
    - \'Sender Account\' is the bank account of shown \'Sender Wallet\'. Enter your debtor's bank account or use \'Sender Wallet\' as bank account
    - \'Receiver Account\' is your own bank account
4. Press \'Export\' to export payments as ISO 20022 camt.054 XML file