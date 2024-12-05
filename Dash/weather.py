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

## Format Data
def format_date(raw_date):
    year = raw_date[:4]
    month = raw_date[4:6]
    day = raw_date[6:8]
    hour = raw_date[8:10]
    minute = raw_date[10:12]
    second = raw_date[12:14]
    return f"{year}-{month}-{day} | {hour}h{minute}min{second}s"

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
                    formatted_dates = [{"label": format_date(date_obj["date"]), "value": date_obj["date"]} for date_obj in dates]
                    return formatted_dates, dates[0]["date"]
        except Exception as e:
            print(f"Error loading dates: {e}")
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
                    html.H2( f"{float(weather_data.get('temperature', 0)) - 273.15:.2f}°C" if weather_data.get('temperature') else "N/A", style={"fontSize": "48px", "color": "#343a40", "textAlign": "center"}),
                    # html.Div(children=[
                    #     html.P("Max / Min", style={"fontWeight": "bold", "color": "#495057"}),
                    #     html.P(f"{weather_data.get('max_temperature', 'N/A')}° / {weather_data.get('min_temperature', 'N/A')}°", style={"color": "#6c757d"})
                    # ]),
                    html.Div(children=[
                        html.P("Humidité", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('humidity', 'N/A')}%", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Pression", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('pression_ocean', 'N/A')} Pa", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                        html.P("Wind Speed", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{float(weather_data.get('wind_speed', 'N/A')):.2f} km/h", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[
                    html.P("Wind Diretion", style={"fontWeight": "bold", "color": "#495057"}),
                    html.P(f"{weather_data.get('wind_direction', 'N/A')}", style={"color": "#6c757d"})
                    ]),

                    html.Div(children=[
                        html.P("Visibilité", style={"fontWeight": "bold", "color": "#495057"}),
                        html.P(f"{weather_data.get('horizontal_visibility', 'N/A')} m", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[ html.P("Point de rosée", style={"fontWeight": "bold", "color": "#495057"}),
                    html.P(f"{float(weather_data.get('dew_point', 0)) - 273.15:.2f}°C" if weather_data.get('dew_point') else "N/A", style={"color": "#6c757d"})
                    ]),
                    html.Div(children=[ html.P("Cloudiness", style={"fontWeight": "bold", "color": "#495057"}),
                    html.P(f"{(weather_data.get('couldiness', 'N/A'))} %", style={"color": "#6c757d"})
                    ]), 
                ]
        except Exception as e:
            print(f"Error querying the API: {e}")
    return [html.P("Data not available", style={"textAlign": "center", "color": "#6c757d"})]

# main
if __name__ == "__main__":
    app.run_server(debug=True)
