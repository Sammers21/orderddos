
import React from "react";
import ReactDOM from "react-dom";

import PositiveIntegerField from "./components/PositiveIntegerField.jsx";
import OptDateTimeField from "./components/OptDateTimeField.jsx";

class OrderForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            email: "",
            targetUrl: "",
            numNa: 10,
            numEu: 0,
            numA: 0,
            duration: 30,
            startTime: null,

            wasSubmitAttempted: false,
            status: 'INITIAL'
        };
    }

    resetStatus() {
        if(this.state.status === 'SUCCESS') {
            this.setState({
                wasSubmitAttempted: false,
                status: 'INITIAL'
            });
        }
    }

    handleUpdate(key, newValue) {
        this.setState({ [key]: newValue }, () => this.resetStatus());
    }

    handleEmailUpdate(newEmail) {
        this.setState({ email: newEmail }, () => this.resetStatus());
    }

    handleTargetUrlUpdate(newTargetUrl) {
        this.setState({ targetUrl: newTargetUrl }, () => this.resetStatus());
    }

    isInputNonzero() {
        if(this.state.numNa === 0 && this.state.numEu === 0 && this.state.numA === 0) {
            return false;
        }

        if(this.state.duration === 0) {
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
        if(!this.isInputNonzero()) {
            return false;
        }

        if(!this.validateEmail()) {
            return false;
        }

        if(!this.validateTargetUrl()) {
            return false;
        }

        return true;
    }

    handleSubmit(e) {
        this.setState({ wasSubmitAttempted: true });

        if(this.isValid()) {
            console.log(this.state);

            this.setState({ status: 'SENDING' });

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
                    duration: this.state.duration,
                    startTime: this.state.startTime
                })
            }).then(response => {
                // if(response.status !== 201) { ... }

                return response.json();
            }).then(data => {
                if(data.status === 'OK') {
                    this.setState({ status: 'SUCCESS' }, () => {
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

    render() {
        return <div className="mx-auto" style={{maxWidth: "600px"}}>
            <p>
                Evaluate DDoS-vulnerability of your <s>competitors</s> servers.
            </p>

            <div className="card" id="form-order">
                <form className="card-body" action="/submit-order" method="POST" onSubmit={e => this.handleSubmit(e)}>
                    <h3 className="text-center mb-4">Order a DDoS attack</h3>

                    <div className="form-group">
                        <div className="form-row">
                            <label className="col-2 col-form-label" htmlFor="email">E-mail</label>
                            <div className="col-10">
                                <div className="input-group">
                                    <input className={"form-control" + ((this.state.wasSubmitAttempted && !this.validateEmail()) ? " is-invalid" : "")}
                                           type="text" id="email" name="email"
                                           autoFocus={true} autoComplete="email"
                                           placeholder="titantins@gmail.com"
                                           value={this.state.email}
                                           onChange={e => this.handleEmailUpdate(e.target.value)} />
                                    <div className="invalid-feedback">
                                      Please provide an E-mail address.
                                    </div>
                                </div>
                            </div>
                        </div>
                        <small className="form-text text-muted">
                            You'll be personally contacted by Pavel Drankov, our CEO.
                        </small>
                    </div>

                    <hr />

                    <div className="form-group form-row">
                        <label className="col-2 col-form-label" htmlFor="target_url">URL</label>
                        <div className="col-10">
                            <div className="input-group">
                                <input className={"form-control" + ((this.state.wasSubmitAttempted && !this.validateTargetUrl()) ? " is-invalid" : "")}
                                       type="text" id="target_url" name="target_url"
                                       autoComplete="url"
                                       placeholder="https://github.com/Sammers21/"
                                       value={this.state.targetUrl}
                                       onChange={e => this.handleTargetUrlUpdate(e.target.value)} />
                                <div className="invalid-feedback">
                                  Please provide a target URL.
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Node count</label>
                        <div className="row">
                            <div className="col">
                                <div className="input-group input-group-sm">
                                    <div className="input-group-prepend">
                                        <label className="input-group-text" htmlFor="num_nodes_na">North America</label>
                                    </div>
                                    <PositiveIntegerField className="form-control form-control-sm" name="num_nodes_na"
                                                          value={this.state.numNa} onInput={v => this.handleUpdate('numNa', v)} />
                                </div>
                            </div>
                            <div className="col">
                                <div className="input-group input-group-sm">
                                    <div className="input-group-prepend">
                                        <label className="input-group-text" htmlFor="num_nodes_eu">Europe</label>
                                    </div>
                                    <PositiveIntegerField className="form-control form-control-sm" name="num_nodes_eu"
                                                           value={this.state.numEu} onInput={v => this.handleUpdate('numEu', v)} />
                                </div>
                            </div>
                            <div className="col">
                                <div className="input-group input-group-sm">
                                    <div className="input-group-prepend">
                                        <label className="input-group-text" htmlFor="num_nodes_a">Asia</label>
                                    </div>
                                    <PositiveIntegerField className="form-control form-control-sm" name="num_nodes_a"
                                                          value={this.state.numA} onInput={v => this.handleUpdate('numA', v)} />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="col form-group">
                            <label htmlFor="start_time">Start time</label>
                            <OptDateTimeField name="start_time" onInput={newDate => this.handleUpdate('startTime', newDate)} />
                        </div>

                        <div className="col form-group">
                            <label htmlFor="duration">Duration</label>
                            <div className="input-group">
                                <PositiveIntegerField className="form-control" name="duration"
                                                      value={this.state.duration} onInput={v => this.handleUpdate('duration', v)}/>
                                <div className="input-group-append">
                                    <label className="input-group-text" htmlFor="duration">minutes</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="form-group form-row">
                        <label className="col-2 col-form-label" htmlFor="target_url">Subtotal</label>
                        <div className="col-10 col-form-label">
                            {
                                this.isInputNonzero() ? <>
                                    <span className="text-muted">
                                        <strong>{this.props.baseCost}</strong>
                                        {(this.state.numNa !== 0) && <>
                                            &nbsp;+&nbsp;
                                            <strong>{this.state.numNa}</strong>&thinsp;&times;&thinsp;<strong>{this.state.duration}</strong>&thinsp;&times;&thinsp;<strong>{this.props.costNa}</strong>
                                        </>}
                                        {(this.state.numEu !== 0) && <>
                                            &nbsp;+&nbsp;
                                            <strong>{this.state.numEu}</strong>&thinsp;&times;&thinsp;<strong>{this.state.duration}</strong>&thinsp;&times;&thinsp;<strong>{this.props.costEu}</strong>
                                        </>}
                                        {(this.state.numA !== 0) && <>
                                            &nbsp;+&nbsp;
                                            <strong>{this.state.numA}</strong>&thinsp;&times;&thinsp;<strong>{this.state.duration}</strong>&thinsp;&times;&thinsp;<strong>{this.props.costA}</strong>
                                        </>}
                                        &nbsp;=&nbsp;
                                    </span>
                                    <strong>{
                                        (
                                            this.props.baseCost
                                                + (this.state.numNa * this.state.duration * this.props.costNa)
                                                + (this.state.numEu * this.state.duration * this.props.costEu)
                                                + (this.state.numA * this.state.duration * this.props.costA)
                                        ).toFixed(2)
                                    }</strong> ₽
                                </> : <span className="text-muted">—</span>
                            }
                        </div>
                    </div>

                    <hr />

                    <div className="text-center">
                        <button type="submit" className={"btn btn-lg " + (this.state.status === 'SUCCESS' ? "btn-success" : "btn-primary") + " px-5 text-center"}
                                disabled={this.state.status !== 'INITIAL' || !this.isInputNonzero() || (this.state.wasSubmitAttempted && !this.isValid())}>
                            {
                                this.state.status === 'SENDING' ? (
                                    <img src="/spinner.svg" style={{'height': '30px'}} />
                                ) : this.state.status === 'SUCCESS' ? (
                                    <img src="/tick.svg" style={{'height': '30px', 'opacity': '0.75'}} />
                                ) : <>Submit</>
                            }
                        </button>
                    </div>
                </form>
            </div> {/* .card */}
        </div>;
    }
}

window.addEventListener('load', () => {
    ReactDOM.render(<OrderForm baseCost={50} costNa={0.09} costEu={0.11} costA={0.15}/>, document.getElementById('order-form-container'));
});
