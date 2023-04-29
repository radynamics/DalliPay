class WalletBridge {
    constructor(displayName, walletWebsiteUrl) {
        this._displayName = displayName
        this._walletWebsiteUrl = walletWebsiteUrl
        this._tx = null
        this._amount = null
        this._ccy = null
    }

    success(txHash) {
        this.hide('cancel')
        this.hide('waiting')
        this.show('success')
        this.report({ data: { txHash: txHash } })
    }
    reject() {
        this.reportRejected('user_rejected', 'User rejected to sign the payment.')
    }

    reportRejected(key, message) {
        this.hide('waiting')
        this.hide('walletApiNotAvailable')
        this.show('rejected')
        this.reportFailure(key, message)
    }
    reportFailure(key, message) {
        console.error(key + ': ' + message)
        this.report({ error: { key: key, message: message } })
    }
    report(json) {
        fetch(window.location, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(json)
        })
    }

    init() {
        document.body.innerHTML = this.createBodyHtml()
        document.getElementById("cancel").addEventListener("click", this.cancel, false);
        document.getElementById("copy").addEventListener("click", this.copyUrlToClipboard, false);

        const urlParams = new URLSearchParams(window.location.search)
        var txBase64 = urlParams.get('tx')
        if(txBase64 == null) {
            this.reportFailure('missing_param', 'GET param tx missing')
            return false
        }
        this._tx = JSON.parse(atob(txBase64))

        this._amount = urlParams.get('amt')
        if(this._amount == null) {
            this.reportFailure('missing_param', 'GET param amt missing')
            return false
        }
        this._ccy = urlParams.get('ccy')
        if(this._ccy == null) {
            this.reportFailure('missing_param', 'GET param ccy missing')
            return false
        }

        let amtText = this.createAmountText()
        document.getElementById('amount').textContent = amtText
        document.title = `${this._displayName}: ${amtText}`
        return true
    }

    createBodyHtml() {
        return `<div id="waiting" style="display: visible;">
                    <h4>Waiting</h4>
                    <p>Waiting for the browser wallet extension to process your request. The extension should appear after a few seconds.</p>
                </div>
                <div id="success" style="display: none;">
                    <h4>✔️Successfully completed</h4>
                    <p>The action has been completed successfully. You can close this window at any time.</p>
                </div>
                <div id="rejected" style="display: none;">
                    <h4>❌ Action rejected</h4>
                    <p>The action was rejected and could not be completed. You can close this window at any time.</p>
                </div>
                <div id="walletApiNotAvailable" style="display: none;">
                    <h4>❌ ` + this._displayName + ` not available</h4>
                    <p>Failed to connect to wallet API. The following steps may help:</p>
                    <ul>
                        <li>Make sure you have the <a href="` + this._walletWebsiteUrl + `" target="_blank">` + this._displayName + `</a> browser extension installed.</li>
                        <li>Make sure you are using a browser that supports this extension. You can <a id="copy" href="#">copy</a> the browser location from the address bar and paste it into another browser.</li>
                    </ul>
                </div>
                <div>
                    <p style="font-style: italic;">Payment: <span id="amount">unknown</span></p>
                    <br />
                    <button id="cancel" class="block">Cancel / Reject</button>
                </div>`
    }
    createAmountText() {
        return `${this._amount} ${this._ccy}`
    }

    hide(id) {
        document.getElementById(id).style.display = 'none';
    }
    show(id) {
        document.getElementById(id).style.display = 'inline';
    }

    cancel() {
        let bridge = new WalletBridge();
        bridge.hide('cancel')
        bridge.reportRejected('user_cancelled', 'User cancelled sign request.')
    }

    copyUrlToClipboard() {
        navigator.clipboard.writeText(window.location);
    }
}