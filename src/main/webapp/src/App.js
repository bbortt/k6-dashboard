// @flow
import {useEffect, useState} from "react";

import { Line } from 'react-chartjs-2';

import './App.css';

const App = () => {
  const [samples, setSamples] = useState([]);

  useEffect(() => {
    fetch('http://localhost:8080/api/rest/v1/k6/samples')
        .then((res) => {
          return res.json();
        })
        .then((data) => {
          console.log(data);
          setSamples(data);
        });
  }, []);

  return (
    <div className="App">
          <Line
              data={samples}
          />
    </div>
  );
}

export default App;
