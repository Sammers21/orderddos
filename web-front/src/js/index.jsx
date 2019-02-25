
import React from "react";
import ReactDOM from "react-dom";

import PositiveIntegerField from "./components/PositiveIntegerField.jsx";
import OptDateTimeField from "./components/OptDateTimeField.jsx";

class OrderForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            numNa: 10,
            numEu: 0,
            numA: 0,
            duration: 30,
            startTime: null
        };
    }

    handleUpdate(key, newValue) {
        this.setState({
            [key]: newValue
        });
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

    render() {
        return (
            <div className="mx-auto" style={{maxWidth: "600px"}}>
                <p>
                    Evaluate DDoS-vulnerability of your <s>competitors</s> servers.
                </p>

                <div className="card" id="form-order">
                    <form className="card-body" action="/submit-order" method="POST">
                        <h3 className="text-center mb-4">Order a DDoS attack</h3>

                        <div className="form-group">
                            <div className="form-row">
                                <label className="col-2 col-form-label" htmlFor="email">E-mail</label>
                                <div className="col-10">
                                    <input className="form-control" type="text" id="email" name="email" placeholder="titantins@gmail.com" />
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
                                <input className="form-control" type="text" id="target_url" name="target_url" placeholder="https://github.com/Sammers21/" />
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
                            <button type="submit" className="btn btn-lg btn-primary px-5" disabled={!this.isInputNonzero()}>Submit</button>
                        </div>
                    </form>
                </div> {/* .card */}
            </div>
        );
    }
}

window.addEventListener('load', () => {
    ReactDOM.render(<OrderForm baseCost={50} costNa={0.09} costEu={0.11} costA={0.15}/>, document.getElementById('order-form-container'));
});
