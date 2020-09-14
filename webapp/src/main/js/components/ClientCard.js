const React = require('react');

class ClientCard extends React.Component {

    constructor(props) {
        super(props);
        this.state = {client: props.client};
    }

    render() {
        return (
            <div className="card shadow mb-4">
                <div className="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 className="m-0 font-weight-bold text-primary"
                        style={{width: 250}}>[{this.state.client.id}][{this.state.client.records}]</h6>
                    <div className="dropdown no-arrow">
                        <a className="dropdown-toggle" href="#" role="button" id="dropdownMenuLink"
                           data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <i className="fas fa-ellipsis-v fa-sm fa-fw text-gray-400"></i>
                        </a>
                        <div className="dropdown-menu dropdown-menu-right shadow animated--fade-in"
                             aria-labelledby="dropdownMenuLink">
                            <div className="dropdown-header">{app.download_reports || "{app.download_reports}"}:</div>
                            <a className="dropdown-item"
                               href={"./report/purchases?id=" + this.state.client.id}>{app.purchases || "{app.purchases}"}</a>
                            <div className="dropdown-divider"></div>
                            <a className="dropdown-item"
                               href={"./report/sales?id=" + this.state.client.id}>{app.sales || "{app.sales}"}</a>
                            <div className="dropdown-divider"></div>
                            <a className="dropdown-item"
                               href={"./report/transactions?id=" + this.state.client.id}>{app.transactions || "{app.transactions}"}</a>
                        </div>
                    </div>
                </div>
                <div className="card-body">
                    <span className="m-0 font-weight-bold text-secondary">{this.state.client.name}</span>
                </div>
            </div>
        );
    }
}


export default class ClientCardList extends React.Component {
    render() {
        const listItems = session.clients.map((client) => <ClientCard client={client} key={client.id}/>);
        return (
            <div className="row">
                {listItems}
            </div>
        );
    }
}