const React = require('react');

export default class Sidebar extends React.Component {
    render() {
        return (
            <ul className="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
                <a className="sidebar-brand d-flex align-items-center justify-content-center" href="./index.html">
                    <div className="sidebar-brand-icon rotate-n-15">
                        <i className="fas fa-laugh-wink"/>
                    </div>
                    <div className="sidebar-brand-text mx-3"><span>{app.title}</span></div>
                </a>
                <hr className="sidebar-divider my-0"/>
                <li className="nav-item">
                    <a className="nav-link" href="./index.html">
                        <i className="fas fa-fw fa-tachometer-alt"></i>
                        <span>{app.dashboard}</span></a>
                </li>
                <hr className="sidebar-divider"/>
                <div className="sidebar-heading"> Interface</div>
                <li className="nav-item">
                    <a aria-controls="collapseTwo" aria-expanded="true" className="nav-link collapsed"
                       data-target="#collapseTwo"
                       data-toggle="collapse" href="#">
                        <i className="fas fa-fw fa-cog"></i>
                        <span>{app.reports}</span>
                    </a>
                    <div aria-labelledby="headingTwo" className="collapse" data-parent="#accordionSidebar"
                         id="collapseTwo">
                        <div className="bg-white py-2 collapse-inner rounded">
                            <h6 className="collapse-header">{app.download_reports}:</h6>
                            <a className="collapse-item" href="./report/purchases">{app.purchases}</a>
                            <a className="collapse-item" href="./report/sales">{app.sales}</a>
                        </div>
                    </div>
                </li>
                <hr className="sidebar-divider d-none d-md-block"/>
                <div className="text-center d-none d-md-inline">
                    <button className="rounded-circle border-0" id="sidebarToggle"></button>
                </div>
            </ul>);
    }

}
