
import React from "react";

class PositiveIntegerField extends React.Component {
    handleChange(e) {
        const newValue = parseInt(e.target.value !== "" ? e.target.value : 0);

        if(isNaN(newValue) || newValue < 0) {
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

export default PositiveIntegerField;
