
import React from "react";
import ReactDOM from "react-dom";

class IntegerInputField extends React.Component {
    handleChange(e) {
        const newValue = parseInt(e.target.value !== "" ? e.target.value : 0);

        if(isNaN(newValue)) {
            return false;
        }

        if(this.props.onInput) {
            this.props.onInput(newValue);
        }
    }

    render() {
        return (
            <input type="text" className={this.props.className} id={this.props.name} name={this.props.name}
                   value={this.props.value} onChange={e => this.handleChange(e)} />
        );
    }
}

class OrderForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            numNa: 10,
            numEu: 0,
            numA: 0,
            duration: 30
        };
    }

    handleUpdate(key, newValue) {
        this.setState({
            [key]: newValue
        });
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
                                        <IntegerInputField className="form-control form-control-sm" name="num_nodes_na"
                                               value={this.state.numNa} onInput={v => this.handleUpdate('numNa', v)} />
                                    </div>
                                </div>
                                <div className="col">
                                    <div className="input-group input-group-sm">
                                        <div className="input-group-prepend">
                                            <label className="input-group-text" htmlFor="num_nodes_eu">Europe</label>
                                        </div>
                                        <IntegerInputField className="form-control form-control-sm" name="num_nodes_eu"
                                               value={this.state.numEu} onInput={v => this.handleUpdate('numEu', v)} />
                                    </div>
                                </div>
                                <div className="col">
                                    <div className="input-group input-group-sm">
                                        <div className="input-group-prepend">
                                            <label className="input-group-text" htmlFor="num_nodes_a">Asia</label>
                                        </div>
                                        <IntegerInputField className="form-control form-control-sm" name="num_nodes_a"
                                               value={this.state.numA} onInput={v => this.handleUpdate('numA', v)} />
                                    </div>
                                </div>
                            </div>
                        </div>

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

                            <div className="col form-group">
                                <label htmlFor="duration">Duration</label>
                                <div className="input-group">
                                    <IntegerInputField className="form-control" name="duration"
                                                       value={this.state.duration} onInput={v => this.handleUpdate('duration', v)}/>
                                    <div className="input-group-append">
                                        <div className="input-group-text">minutes</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="form-group form-row">
                            <label className="col-2 col-form-label" htmlFor="target_url">Subtotal</label>
                            <div className="col-10 col-form-label">
                                <span className="text-muted">
                                    <strong>2</strong> &times; <strong>2</strong> =
                                </span>
                                &nbsp;
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
