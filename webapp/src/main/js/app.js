const React = require('react');
const ReactDOM = require('react-dom');
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';
import ClientCardList from './components/ClientCard';
import ImportAccountCardList from './components/ImportAccountCard';

class App extends React.Component {
    render() {
        return [
            <Sidebar key={"sidebar"}/>,
            <div id="content-wrapper" className="d-flex flex-column">
                <div id="content">
                    <Navbar key={"navbar"}/>
                    <div className="container-fluid">
                        <div className="d-sm-flex align-items-center justify-content-between mb-4">

                        </div>
                        <ImportAccountCardList key={"importAccountList"}/>
                        <ClientCardList key={"clientCardList"}/>
                    </div>
                </div>
                <footer className="sticky-footer bg-white">
                    <div className="container my-auto">
                        <div className="copyright text-center my-auto">
                            <span>Copyright &copy; ekonomi.julatec.name 2020</span>
                        </div>
                    </div>
                </footer>
            </div>,
        ]
    }
}

ReactDOM.render(<App key={"ekonomi"}/>,
    document.getElementById('wrapper')
);
