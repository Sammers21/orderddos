
import React from "react";

const getNextHour = () => {
    const nextHour = new Date();
    nextHour.setHours(nextHour.getHours() + 1);
    nextHour.setMinutes(0);
    nextHour.setSeconds(0);

    return nextHour;
};

const dateToISO = date => {
    const d = new Date(date);

    const zeroPad = (x, l) => {
        x = x.toString();

        while(x.length < l) {
            x = '0' + x;
        }

        return x;
    };

    return zeroPad(d.getFullYear(), 4)
        + '-' + zeroPad(d.getMonth() + 1, 2)
        + '-' + zeroPad(d.getDate(), 2)
        + 'T' + zeroPad(d.getHours(), 2)
        + ':' + zeroPad(d.getMinutes(), 2);
};

const maxStartDate = () => {
    const d = new Date();
    d.setMonth(d.getMonth() + 1);

    return d;
}

class OptDateTimeField extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            enabled: false,
            value: dateToISO(getNextHour())
        };

        // TODO: highlight invalid, non-called-back value
    }

    dateValue() {
        return new Date(this.state.value);
    }

    handleEnabledChange(newValue) {
        this.setState({ enabled: newValue });

        this.props.onInput(newValue ? this.dateValue() : null);
    }

    handleDateTimeChange(newValue) {
        this.setState({ value: newValue }, () => {
            if(new Date() <= this.dateValue() && this.dateValue() < maxStartDate()) {
                this.props.onInput(this.dateValue());
            }
        });
    }

    handleDoubleClick() {
        if(!this.state.enabled) {
            this.handleEnabledChange(true);
        }

        return true;
    }

    handleFocus() {
        if(this.dateValue() < new Date()) {
            this.setState({ value: dateToISO(getNextHour()) })
        }
    }

    handleBlur() {
        let subValue = null;

        if(this.dateValue() < new Date()) {
            subValue = getNextHour();
        }
        else if(maxStartDate() < this.dateValue()) {
            subValue = maxStartDate();
        }

        if(subValue === null) {
            this.props.onInput(this.dateValue());
        }
        else {
            this.setState({ value: dateToISO(subValue) }, () => {
                this.props.onInput(subValue);
            });
        }
    }

    render() {
        return (
            <div className="input-group" onDoubleClick={() => this.handleDoubleClick()}>
                <div className="input-group-prepend">
                    <label className="input-group-text">
                        <input type="checkbox" checked={this.state.enabled}
                               onChange={e => this.handleEnabledChange(e.target.checked)} />
                   </label>
                </div>
                <input className="form-control" type="datetime-local" id={this.props.name} name={this.props.name}
                       disabled={!this.state.enabled}
                       value={this.state.value}
                       min={dateToISO(new Date())} max={dateToISO(maxStartDate())}
                       onChange={e => this.handleDateTimeChange(e.target.value)}
                       onFocus={() => this.handleFocus()}
                       onBlur={() => this.handleBlur()} />
            </div>
        );
    }
}

export default OptDateTimeField;
