const React = require('react');
import DragAndDrop from './DragAndDrop';

class ImportAccountCard extends React.Component {


    constructor(props) {
        super(props);
        this.state = {
            name: props.name,
            endpoint: props.endpoint
        };
    }

    handleDrop(files) {
        for (var i = 0; i < files.length; i++) {
            if (!files[i].name) return;
            var formData = new FormData();
            formData.append("file", files[i]);
            alert("Importing...");
            $.ajax({
                url: this.props.endpoint,
                method: "POST",
                type: "POST",
                dataType: "multipart/form-data",
                data: formData,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data, text) {
                    alert("Respuesta: " + text);
                },
                error: function (request, status, error) {
                    if (request.status != 200) {
                        alert(JSON.parse(request.responseText).message);
                    } else {
                        alert(request.responseText);
                    }
                }
            })
            //     .done(function (res) {
            //     alert("Respuesta: " + res);
            // })
            ;
        }
    }

    render() {
        return (

            <div className="col-xl-3 col-md-6 mb-4">
                <div className="card border-left-success shadow h-100 py-2">
                    <DragAndDrop key={"fileDrop"} handleDrop={this.handleDrop.bind(this)}>
                        <div className="card-body">

                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        {this.props.name}
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">

                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-dollar-sign fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </DragAndDrop>
                </div>
            </div>
        );
    }
}


export default class ImportAccountCardList extends React.Component {
    render() {
        const listItems = session.importAccounts.map((account) =>
            <ImportAccountCard endpoint={account.endpoint} key={account.key} name={account.name}/>);
        return (
            <div className="row">
                {listItems}
            </div>
        );
    }
}