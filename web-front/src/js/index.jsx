
import React from "react";
import ReactDOM from "react-dom";

class NodeCountFields extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            na: 10,
            eu: 0,
            a: 0
        }
    }

    handleNumUpdate(key, e) {
        const newValue = parseInt(e.target.value !== "" ? e.target.value : 0);

        if(isNaN(newValue)) {
            return false;
        }

        this.setState({
            [key]: newValue
        });

        if(this.onValueUpdate) {
            this.onValueUpdate();
        }
    }

    render() {
        return (
            <div className="form-group">
                <label>Node count</label>
                <div className="row">
                    <div className="col">
                        <div className="input-group input-group-sm">
                            <div className="input-group-prepend">
                                <label className="input-group-text" htmlFor="num_nodes_na">North America</label>
                            </div>
                            <input className="form-control form-control-sm" type="text" name="num_nodes_na"
                                   value={this.state.na} onChange={e => this.handleNumUpdate('na', e)} />
                        </div>
                    </div>
                    <div className="col">
                        <div className="input-group input-group-sm">
                            <div className="input-group-prepend">
                                <label className="input-group-text" htmlFor="num_nodes_eu">Europe</label>
                            </div>
                            <input className="form-control form-control-sm" type="text" name="num_nodes_eu"
                                   value={this.state.eu} onChange={e => this.handleNumUpdate('eu', e)} />
                        </div>
                    </div>
                    <div className="col">
                        <div className="input-group input-group-sm">
                            <div className="input-group-prepend">
                                <label className="input-group-text" htmlFor="num_nodes_a">Asia</label>
                            </div>
                            <input className="form-control form-control-sm" type="text" name="num_nodes_a"
                                   value={this.state.a} onChange={e => this.handleNumUpdate('a', e)} />
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

class DurationField extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            value: 30
        };
    }

    handleChange(e) {
        const newValue = parseInt(e.target.value !== "" ? e.target.value : 0);

        if(isNaN(newValue)) {
            return false;
        }

        this.setState({
            value: newValue
        });

        if(this.onValueUpdate) {
            this.onValueUpdate();
        }
    }

    render() {
        return (
            <div className="col form-group">
                <label htmlFor="duration">Duration</label>
                <div className="input-group">
                    <input className="form-control" type="text" id="duration" name="duration"
                           value={this.state.value} onChange={e => this.handleChange(e)} />
                    <div className="input-group-append">
                        <div className="input-group-text">minutes</div>
                    </div>
                </div>
            </div>
        );
    }
}

class OrderForm extends React.Component {
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

                        <NodeCountFields/>

                        <div className="form-row">
                            <div className="col form-group">
                                <label htmlFor="start_time">Start time</label>
                                <div className="input-group">
                                    <div className="input-group-prepend">
                                        <div className="input-group-text"><input type="checkbox" name="" /></div>
                                    </div>
                                    <input className="form-control" type="datetime-local" id="start_time" name="start_time" placeholder="2010-10-10 10:10:10" disabled />
                                </div>
                            </div>
                            <DurationField/>
                        </div>

                        <div className="form-group form-row">
                            <label className="col-2 col-form-label" htmlFor="target_url">Subtotal</label>
                            <div className="col-10 col-form-label">
                                <span className="text-muted">
                                    <strong>2</strong> &times; <strong>2</strong> =
                                </span>
                                <strong>14.88</strong> ₽
                            </div>
                        </div>

                        <hr />

                        <div className="text-center">
                            <button type="submit" className="btn btn-lg btn-primary px-5">Submit</button>
                        </div>
                    </form>
                </div> {/* .card */}
            </div>
        );
    }
}

window.addEventListener('load', () => {
    ReactDOM.render(<OrderForm/>, document.getElementById('order-form-container'));
});
