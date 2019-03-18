import React from "react";
import ReactDOM from "react-dom";

import PositiveIntegerField from "./components/PositiveIntegerField.jsx";
import OptDateTimeField from "./components/OptDateTimeField.jsx";

// TODO adjust the variable
// $ 0.01 per GB. Single node bandwidth ~ 1 Gbit/s
const NODE_COST_PER_SECOND = 0.01 / 8;
const EFFECTIVENESS_RATIO = 0.75;
const NODE_COST_PER_MINUTE = NODE_COST_PER_SECOND * 60 * EFFECTIVENESS_RATIO;

class OrderForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            email: "",
            targetUrl: "",
            numNa: 10,
            numEu: 0,
            numA: 0,
            duration_hours: 0,
            duration_minutes: 0,
            startTime: null,
            wasSubmitAttempted: false,
            status: 'INITIAL'
        };
    }

    resetStatus() {
        if (this.state.status === 'SUCCESS') {
            this.setState({
                wasSubmitAttempted: false,
                status: 'INITIAL'
            });
        }
    }

    handleUpdate(key, newValue) {
        this.setState({[key]: newValue}, () => this.resetStatus());
    }

    handleEmailUpdate(newEmail) {
        this.setState({email: newEmail}, () => this.resetStatus());
    }

    handleTargetUrlUpdate(newTargetUrl) {
        this.setState({targetUrl: newTargetUrl}, () => this.resetStatus());
    }

    isInputNonzero() {
        if (this.state.numNa === 0 && this.state.numEu === 0 && this.state.numA === 0) {
            return false;
        }

        if (this.state.duration === 0) {
            return false;
        }

        return true;
    }

    validateEmail() {
        return this.state.email.match('[^@]+@[^@]+');
    }

    validateTargetUrl() {
        return this.state.targetUrl.match(/\S/);
    }

    isValid() {
        if (!this.isInputNonzero()) {
            return false;
        }

        if (!this.validateEmail()) {
            return false;
        }

        if (!this.validateTargetUrl()) {
            return false;
        }

        return true;
    }

    nodeCount() {
        return this.state.numEu + this.state.numA + this.state.numNa;
    }

    money() {
        return Math.round(this.nodeCount() * this.durationMinutes() * NODE_COST_PER_MINUTE * 100) / 100
    }

    handleSubmit(e) {
        this.setState({wasSubmitAttempted: true});

        if (this.isValid()) {
            console.log(this.state);

            this.setState({status: 'SENDING'});

            fetch("/submit-order", {
                method: 'POST',
                headers: {
                    'Content-Type': "application/json"
                },
                body: JSON.stringify({
                    email: this.state.email,
                    targetUrl: this.state.targetUrl,
                    numNa: this.state.numNa,
                    numEu: this.state.numEu,
                    numA: this.state.numA,
                    duration_hours: this.state.duration_hours,
                    duration_minutes: this.state.duration_minutes,
                    startTime: this.state.startTime
                })
            }).then(response => {
                // if(response.status !== 201) { ... }

                return response.json();
            }).then(data => {
                if (data.status === 'OK') {
                    this.setState({status: 'SUCCESS'}, () => {
                        setTimeout(() => {
                            location.href = data.location;
                        }, 500);
                    });
                }

                // TODO: return and process errors
            }).catch(err => {
                console.error("ERROR", err);
            });
        }

        e.preventDefault();
        return false;
    }

    durationMinutes() {
        return this.state.duration_hours * 60 + this.state.duration_minutes;
    }

    render() {
        return <div className="mx-auto" style={{maxWidth: "600px"}}>

            <div className="card" id="form-order">
                <form className="card-body" action="/submit-order" method="POST" onSubmit={e => this.handleSubmit(e)}>
                    <h3 className="text-center mb-4">DDoS attack order form</h3>

                    <div className="form-group">
                        <div className="form-row">
                            <label className="col-2 col-form-label" htmlFor="email">E-mail</label>
                            <div className="col-10">
                                <div className="input-group">
                                    <input
                                        className={"form-control" + ((this.state.wasSubmitAttempted && !this.validateEmail()) ? " is-invalid" : "")}
                                        type="text" id="email" name="email"
                                        autoFocus={false} autoComplete="email"
                                        placeholder="example@example.net"
                                        value={this.state.email}
                                        onChange={e => this.handleEmailUpdate(e.target.value)}/>
                                    <div className="invalid-feedback">
                                        Please provide an E-mail address.
                                    </div>
                                </div>
                            </div>
                        </div>
                        <small className="form-text text-muted">
                            You'll be contacted in 3 business days in order to verify the resource ownership.
                        </small>
                    </div>

                    <hr/>

                    <div className="form-group form-row">
                        <label className="col-2 col-form-label" htmlFor="target_url">URL</label>
                        <div className="col-10">
                            <div className="input-group">
                                <input
                                    className={"form-control" + ((this.state.wasSubmitAttempted && !this.validateTargetUrl()) ? " is-invalid" : "")}
                                    type="text" id="target_url" name="target_url"
                                    autoComplete="url"
                                    placeholder="https://exmple.com/"
                                    value={this.state.targetUrl}
                                    onChange={e => this.handleTargetUrlUpdate(e.target.value)}/>
                                <div className="invalid-feedback">
                                    Please provide a target URL.
                                </div>
                            </div>
                        </div>
                        <small className="form-text text-muted">
                            The recourse URL to perform a DDoS attack.
                        </small>
                    </div>

                    <div className="form-group">
                        <label>Nodes count per region</label>
                        <div className="row">
                            <div className="col">
                                <div className="input-group input-group-sm">
                                    <div className="input-group-prepend">
                                        <label className="input-group-text" htmlFor="num_nodes_na">North America</label>
                                    </div>
                                    <PositiveIntegerField className="form-control form-control-sm" name="num_nodes_na"
                                                          value={this.state.numNa}
                                                          onInput={v => this.handleUpdate('numNa', v)}/>
                                </div>
                            </div>
                            <div className="col">
                                <div className="input-group input-group-sm">
                                    <div className="input-group-prepend">
                                        <label className="input-group-text" htmlFor="num_nodes_eu">Europe</label>
                                    </div>
                                    <PositiveIntegerField className="form-control form-control-sm" name="num_nodes_eu"
                                                          value={this.state.numEu}
                                                          onInput={v => this.handleUpdate('numEu', v)}/>
                                </div>
                            </div>
                            <div className="col">
                                <div className="input-group input-group-sm">
                                    <div className="input-group-prepend">
                                        <label className="input-group-text" htmlFor="num_nodes_a">Asia</label>
                                    </div>
                                    <PositiveIntegerField className="form-control form-control-sm" name="num_nodes_a"
                                                          value={this.state.numA}
                                                          onInput={v => this.handleUpdate('numA', v)}/>
                                </div>
                            </div>
                        </div>
                        <small className="form-text text-muted">
                            Choose the amount and location of nodes used for the attack.
                        </small>
                        <small className="form-text text-muted">
                            Single node bandwidth ~ 1 Gbit/s.
                        </small>
                    </div>

                    <div className="form-row">
                        <label>DDoS attack duration</label>
                        <div className="row">
                            <div className="col form-group">
                                <div className="input-group">
                                    <PositiveIntegerField className="form-control" name="duration"
                                                          value={this.state.duration_hours}
                                                          onInput={v => this.handleUpdate('duration_hours', v)}/>
                                    <div className="input-group-append">
                                        <label className="input-group-text" htmlFor="duration">hours</label>
                                    </div>
                                </div>
                            </div>
                            <div className="col form-group">
                                <div className="input-group">
                                    <PositiveIntegerField className="form-control" name="duration"
                                                          value={this.state.duration_minutes}
                                                          onInput={v => this.handleUpdate('duration_minutes', v)}/>
                                    <div className="input-group-append">
                                        <label className="input-group-text" htmlFor="duration">minutes</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="form-group form-row">
                        <div className="col-10 col-form-label">
                            {
                                this.isInputNonzero() ? <>
                                    Subtotal: ${this.money()}
                                </> : <span className="text-muted">Subtotal: 0</span>
                            }
                        </div>
                    </div>

                    <hr/>

                    <div className="text-center">
                        <button type="submit"
                                className={"btn btn-lg " + (this.state.status === 'SUCCESS' ? "btn-success" : "btn-primary") + " px-5 text-center"}
                                disabled={this.state.status !== 'INITIAL' || !this.isInputNonzero() || (this.state.wasSubmitAttempted && !this.isValid())}>
                            {
                                this.state.status === 'SENDING' ? (
                                    <img src="/spinner.svg" style={{'height': '30px'}}/>
                                ) : this.state.status === 'SUCCESS' ? (
                                    <img src="/tick.svg" style={{'height': '30px', 'opacity': '0.75'}}/>
                                ) : <>Submit</>
                            }
                        </button>
                    </div>
                </form>
            </div>
            {/* .card */}
        </div>;
    }
}

window.addEventListener('load', () => {
    ReactDOM.render(<OrderForm baseCost={50} costNa={0.09} costEu={0.11}
                               costA={0.15}/>, document.getElementById('order-form-container'));
});
