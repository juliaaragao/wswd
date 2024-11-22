import dash
from dash import dcc, html, Input, Output
import dash_bootstrap_components as dbc

# Dados fictícios para os locais e suas informações meteorológicas
locations = {
    "Technopole, Finistère": [
        {"date": "11/11/23", "temperature": "19°", "humidity": "72%", "wind_speed": "31 km/h"},
        {"date": "12/12/23", "temperature": "12°", "humidity": "70%", "wind_speed": "28 km/h"},
        {"date": "12/01/24", "temperature": "10°", "humidity": "68%", "wind_speed": "25 km/h"},
    ],
    "Paris, Île-de-France": [
        {"date": "11/11/23", "temperature": "15°", "humidity": "65%", "wind_speed": "20 km/h"},
        {"date": "12/12/23", "temperature": "10°", "humidity": "60%", "wind_speed": "18 km/h"},
    ],
    "Lyon, Auvergne-Rhône-Alpes": [
        {"date": "11/11/23", "temperature": "17°", "humidity": "60%", "wind_speed": "25 km/h"},
        {"date": "12/12/23", "temperature": "16°", "humidity": "58%", "wind_speed": "22 km/h"},
    ],
    "Marseille, Provence-Alpes-Côte d'Azur": [
        {"date": "11/11/23", "temperature": "20°", "humidity": "70%", "wind_speed": "30 km/h"},
        {"date": "12/12/23", "temperature": "19°", "humidity": "68%", "wind_speed": "28 km/h"},
    ],
}

# Inicializar o app Dash com um tema do Bootstrap
app = dash.Dash(__name__, external_stylesheets=[dbc.themes.COSMO])

# Layout do dashboard
app.layout = dbc.Container([
    html.H1("Météo Dashboard", className="text-center my-4"),

    dbc.Row([
        # Dropdown para selecionar a localização
        dbc.Col([
            html.Label("Selecione a Localização:"),
            dcc.Dropdown(
                id="location-dropdown",
                options=[{"label": loc, "value": loc} for loc in locations.keys()],
                value="Technopole, Finistère",
                clearable=False,
                style={"width": "100%"}
            ),
        ], width=6),

        # Dropdown para selecionar a data
        dbc.Col([
            html.Label("Selecione a Data:"),
            dcc.Dropdown(
                id="date-dropdown",
                clearable=False,
                style={"width": "100%"}
            ),
        ], width=6),
    ], className="mb-4"),

    # Div para exibir as informações meteorológicas
    dbc.Row([
        dbc.Col([
            dbc.Card(
                dbc.CardBody([
                    html.H3(id="weather-title", className="card-title text-center"),
                    html.P(id="weather-temperature", className="card-text text-center"),
                    html.P(id="weather-humidity", className="card-text text-center"),
                    html.P(id="weather-wind", className="card-text text-center"),
                ]),
                className="shadow-sm"
            )
        ], width=12)
    ])
], fluid=True)


# Callback para atualizar as opções de data com base no local selecionado
@app.callback(
    Output("date-dropdown", "options"),
    Input("location-dropdown", "value")
)
def update_date_options(selected_location):
    dates = locations[selected_location]
    return [{"label": entry["date"], "value": entry["date"]} for entry in dates]


# Callback para atualizar as informações meteorológicas com base na localização e data selecionadas
@app.callback(
    [Output("weather-title", "children"),
     Output("weather-temperature", "children"),
     Output("weather-humidity", "children"),
     Output("weather-wind", "children")],
    [Input("location-dropdown", "value"),
     Input("date-dropdown", "value")]
)
def update_weather_info(selected_location, selected_date):
    if not selected_date:
        return "Por favor, selecione uma data.", "", "", ""

    weather_data = next(item for item in locations[selected_location] if item["date"] == selected_date)
    title = f"Météo pour {selected_location} ({selected_date})"
    temperature = f"Temperatura: {weather_data['temperature']}"
    humidity = f"Umidade: {weather_data['humidity']}"
    wind = f"Velocidade do Vento: {weather_data['wind_speed']}"
    return title, temperature, humidity, wind


# Executar o servidor
if __name__ == "__main__":
    app.run_server(debug=True)
