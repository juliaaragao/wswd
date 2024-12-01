from dash import Dash, html, dcc, Input, Output
import requests

# app
app = Dash(__name__)

# URL API
API_BASE_URL = "http://localhost:8080/api/weather"

# dashboard
app.layout = html.Div(
    style={
        "fontFamily": "Arial, sans-serif",
        "width": "70%",
        "margin": "auto",
        "backgroundColor": "#f8f9fa",
        "padding": "20px",
        "borderRadius": "10px",
        "boxShadow": "0px 4px 10px rgba(0, 0, 0, 0.1)"
    },
    children=[
        html.H1("Météo", style={"textAlign": "center", "color": "#343a40"}),
        html.Div(
            [
                html.Label("Station", style={"fontWeight": "bold", "color": "#495057"}),
                dcc.Dropdown(id="station-dropdown", placeholder="Pick a station", style={"marginBottom": "20px"}),
            ]
        ),
        html.Div(
            [
                html.Label("Date", style={"fontWeight": "bold", "color": "#495057"}),
                dcc.Dropdown(id="date-dropdown", placeholder="Pick a date", style={"marginBottom": "20px"}),
            ]
        ),
        html.Div(
            id="weather-info",
            style={
                "display": "grid",
                "gridTemplateColumns": "repeat(3, 1fr)",
                "gap": "10px",
                "marginTop": "20px",
                "backgroundColor": "#ffffff",
                "borderRadius": "10px",
                "padding": "15px",
                "boxShadow": "0px 4px 10px rgba(0, 0, 0, 0.1)"
            }
        ),
    ]
)
# Callback stations
@app.callback(Output("station-dropdown", "options"), Input("station-dropdown", "id"))
def load_stations(_):
    try:
        response = requests.get(f"{API_BASE_URL}/stations")
        if response.status_code == 200:
            stations = response.json()
            return [{"label": station["stationName"], "value": station["station"]} for station in stations]
    except Exception as e:
        print(f"Error loading stations: {e}")
    return []

# Callback dates - stations
@app.callback(
    Output("date-dropdown", "options"),
    Output("date-dropdown", "value"),
    Input("station-dropdown", "value"),
)
def load_dates(station_id):
    if station_id:
        try:
            response = requests.get(f"{API_BASE_URL}/{station_id}")
            if response.status_code == 200:
                dates = response.json()
                if dates:
                    return [{"label": date_obj["date"], "value": date_obj["date"]} for date_obj in dates], dates[0]["date"]
        except Exception as e:
            print(f"rror loading dates: {e}")
    return [], None

# Callback data meteo
@app.callback(
    Output("weather-info", "children"),
    Input("station-dropdown", "value"),
    Input("date-dropdown", "value"),
)
def update_weather_info(station_id, date):
    if station_id and date:
        try:
            response = requests.get(f"{API_BASE_URL}/{station_id}/{date}")
            if response.status_code == 200:
                weather_data = response.json()
                return [
                    html.Div(style={"gridColumn": "span 3"}, children=[
                        html.H2(f"{weather_data.get('temperature', 'N/A')}°", style={"fontSize": "48px", "color": "#343a40", "textAlign": "center"}),
                        html.P("T. ressentie", style={"textAlign": "center", "color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Max / Min", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('max_temperature', 'N/A')}° / {weather_data.get('min_temperature', 'N/A')}°", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Humidité", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('humidity', 'N/A')}%", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Pression", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('pression_ocean', 'N/A')} mb", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Vent", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('wind_speed', 'N/A')} km/h", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Visibilité", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('horizontal_visibility', 'N/A')} km", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Point de rosée", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('dew_point', 'N/A')}°", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Indice UV", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('uv_index', 'N/A')}", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Phase de lune", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('moon_phase', 'N/A')}", style={"color": "#6c757d"})
                    ]),
                ]
        except Exception as e:
            print(f"Error querying the API: {e}")
    return [html.P("Data not available", style={"textAlign": "center", "color": "#6c757d"})]

# main
if __name__ == "__main__":
    app.run_server(debug=True)
