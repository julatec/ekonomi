const React = require('react');

export default class Navbar extends React.Component {

    constructor(props) {
        super(props);
        this.state = {employees: []};
    }

    handleLowerOnChange(e) {
        document.cookie = "rangeLower" + "=" + e.target.valueAsDate.toISOString();
    }

    handleUpperOnChange(e) {
        document.cookie = "rangeUpper" + "=" + e.target.valueAsDate.toISOString();
    }

    handleTenantOnChange(e) {
        document.cookie = "tenant" + "=" + e.target.value;
        window.location.reload();
    }

    render() {

        const tenantItems = session.tenants.map((tenant) =>
            <option value={tenant} key={"tenantOpcion_" + tenant}>{tenant}</option>);

        return (<nav className="navbar navbar-expand navbar-light bg-white topbar mb-4 static-top shadow">
            <button className="btn btn-link d-md-none rounded-circle mr-3" id="C">
                <i className="fa fa-bars"></i>
            </button>
            <form name={"navbar_search"} id={"navbar_search"}
                  className="d-none d-sm-inline-block form-inline mr-auto ml-md-3 my-2 my-md-0 mw-100 navbar-search">
                <div className="input-group">
                    <input aria-describedby="basic-addon2" aria-label="Search"
                           className="form-control bg-light border-0 small"
                           placeholder={app.search_for} type="text"/>
                    <div className="input-group-append">
                        <button className="btn btn-primary" type="button">
                            <i className="fas fa-search fa-sm"></i>
                        </button>
                    </div>
                </div>
            </form>
            <form
                className="d-none d-sm-inline-block form-inline mr-auto ml-md-3 my-2 my-md-0 mw-100 navbar-search">

                <div className="input-group">
                    <input className="form-control bg-light border-0 small"
                           type="date" name="lowerDate"
                           onChange={event => this.handleLowerOnChange(event)}
                           defaultValue={new Date(session.lowerDate).toISOString().slice(0, 10)}/>
                    ::
                    <input className="form-control bg-light border-0 small"
                           type="date" name="upperDate"
                           onChange={event => this.handleUpperOnChange(event)}
                           defaultValue={new Date(session.upperDate).toISOString().slice(0, 10)}/>
                    ::
                    <label htmlFor="tenantSelect">{app.tenant || "{app.tenant}"}:</label>
                    <select id="tenantSelect"
                            name="tenantSelect"
                            key={"tenantSelect"}
                            value={session.tenant}
                            onChange={event => this.handleTenantOnChange(event)}>
                        {tenantItems}
                    </select>

                </div>
            </form>
            <ul className="navbar-nav ml-auto">
                <div className="topbar-divider d-none d-sm-block"></div>
                <li className="nav-item dropdown no-arrow">
                    <a aria-expanded="false" aria-haspopup="true" className="nav-link dropdown-toggle"
                       data-toggle="dropdown"
                       href="#" id="userDropdown" role="button">
                        <span className="mr-2 d-none d-lg-inline text-gray-600 small">{session.username}</span>
                    </a>
                </li>
            </ul>
        </nav>)
    }
}
