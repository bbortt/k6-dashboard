import os

import dash
import pandas as pd
import plotly.graph_objs as go
from dash import dcc, html
from dash.dependencies import Input, Output
from dotenv import load_dotenv
from psycopg2 import pool

load_dotenv()

db_pool = pool.SimpleConnectionPool(minconn=1, maxconn=10,
                                    dbname=os.getenv('DB_NAME', 'k6_dashboard'),
                                    user=os.getenv('DB_USER', 'k6_dashboard'),
                                    password=os.getenv('DB_PASSWORD', 'KrPPCHdYSXz6wMct5tUK'),
                                    host=os.getenv('DB_HOST', 'localhost'))


def fetch_data(query, params=None):
    # Get a connection from the pool
    connection = db_pool.getconn()
    try:
        cursor = connection.cursor()
        if params:
            cursor.execute(query, params)
        else:
            cursor.execute(query)
        result = cursor.fetchall()
    finally:
        cursor.close()
        # Put the connection back in the pool instead of closing it
        db_pool.putconn(connection)
    return result


# Fetch unique test run IDs for dropdown options
test_run_ids_query = "SELECT DISTINCT tags->>'testid' AS testid FROM samples"
test_run_ids = fetch_data(test_run_ids_query)
test_run_id_options = [{'label': id[0], 'value': id[0]} for id in test_run_ids]

app = dash.Dash('K6 Dashboard')

app.layout = html.Div([
    html.H1('K6 DASH Board'),
    dcc.Dropdown(
        id='test-run-dropdown',
        options=test_run_id_options,
        # Set default value to the first test run ID
        value=[test_run_id_options[0]['value']] if test_run_id_options else None,
        # Enable multiple selections
        multi=True
    ),
    # Div to hold the dynamically generated graphs
    html.Div(id='response-time-graph')
])

test_run_query = """
    SELECT date_trunc('second', ts)                            AS time,
           avg(value)                                          AS avg_rt,
           percentile_cont(0.95) WITHIN GROUP (ORDER BY value) AS perc_95_rt
    FROM samples
    WHERE metric = 'http_req_duration'
      AND (tags ->> 'status')::integer < 400
      AND tags ->> 'testid' = %s
    GROUP BY 1
    ORDER BY 1;
    """


@app.callback(
    Output('response-time-graph', 'children'),
    [Input('test-run-dropdown', 'value')]
)
def update_graph(selected_test_run_ids):
    graphs = []  # List to hold the graph components

    for test_run_id in selected_test_run_ids:
        data = fetch_data(test_run_query, (test_run_id,))

        # Convert query results to a DataFrame
        agg_df = pd.DataFrame(data, columns=['time', 'avg_rt', 'perc_95_rt'])

        # Create a Plotly graph object for the average and 95th percentile response times
        figure = go.Figure()
        figure.add_trace(
            go.Scatter(x=agg_df['time'], y=agg_df['avg_rt'], mode='lines+markers', name='Avg Response Time (ms)'))
        figure.add_trace(go.Scatter(x=agg_df['time'], y=agg_df['perc_95_rt'], mode='lines+markers',
                                    name='95th Percentile Response Time (ms)'))
        figure.update_layout(title=f'Response Time for Test Run {test_run_id}', xaxis_title='Timestamp',
                             yaxis_title='Response Time (ms)')

        # Append the figure wrapped in a dcc.Graph component to the graphs list
        graphs.append(dcc.Graph(figure=figure))

    return graphs  # Return the list of Graph components to be dynamically displayed


if __name__ == '__main__':
    app.run_server(debug=True)
