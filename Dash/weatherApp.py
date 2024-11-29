import dash
from dash import dcc, html, Input, Output
import dash_bootstrap_components as dbc
import requests

# 初始化 Dash 应用程序
app = dash.Dash(__name__, external_stylesheets=[dbc.themes.COSMO])

# 应用布局
app.layout = dbc.Container([
    html.H1("Météo Dashboard", className="text-center my-4"),

    dbc.Row([
        # 站点选择
        dbc.Col([
            html.Label("选择站点:"),
            dcc.Dropdown(
                id="location-dropdown",
                options=[],  # 动态加载
                placeholder="选择一个站点",
                clearable=False,
                style={"width": "100%"}
            ),
        ], width=6),

        # 日期选择
        dbc.Col([
            html.Label("选择日期:"),
            dcc.Dropdown(
                id="date-dropdown",
                options=[],  # 动态加载
                placeholder="选择一个日期",
                clearable=False,
                style={"width": "100%"}
            ),
        ], width=6),
    ], className="mb-4"),

    # 显示天气信息
    dbc.Row([
        dbc.Col([
            dbc.Card(
                dbc.CardBody([
                    html.H3(id="weather-title", className="card-title text-center"),
                    html.P(id="weather-temperature", className="card-text text-center"),
                    html.P(id="weather-pression-ocean", className="card-text text-center"),
                    html.P(id="weather-wind-direction", className="card-text text-center"),
                    html.P(id="weather-wind-speed", className="card-text text-center"),
                    html.P(id="weather-dew-point", className="card-text text-center"),
                    html.P(id="weather-humidity", className="card-text text-center"),
                    html.P(id="weather-horizontal-visibility", className="card-text text-center"),
                    html.P(id="weather-couldiness", className="card-text text-center"),
                    html.P(id="weather-min-temperature", className="card-text text-center"),
                    html.P(id="weather-max-temperature", className="card-text text-center"),
                ]),
                className="shadow-sm"
            )
        ], width=12)
    ])
], fluid=True)


# 动态加载站点列表
@app.callback(
    Output("location-dropdown", "options"),
    Input("location-dropdown", "value")
)
def update_location_options(_):
    try:
        response = requests.get("http://localhost:8080/api/weather/stations")  # 假设后端有此端点
        if response.status_code == 200:
            stations = response.json()
            return [{"label": station, "value": station} for station in stations]
        return []
    except Exception as e:
        print(f"Error fetching stations: {e}")
        return []


# 动态加载日期列表
@app.callback(
    Output("date-dropdown", "options"),
    Input("location-dropdown", "value")
)
def update_date_options(selected_location):
    if not selected_location:
        return []
    try:
        response = requests.get(f"http://localhost:8080/api/weather/{selected_location}")  # 假设后端支持
        if response.status_code == 200:
            dates = response.json()
            return [{"label": date, "value": date} for date in dates]
        return []
    except Exception as e:
        print(f"Error fetching dates: {e}")
        return []


# 更新天气信息
@app.callback(
    [Output("weather-title", "children"),
     Output("weather-temperature", "children"),
     Output("weather-pression-ocean", "children"),
     Output("weather-wind-direction", "children"),
     Output("weather-wind-speed", "children"),
     Output("weather-dew-point", "children"),
     Output("weather-humidity", "children"),
     Output("weather-horizontal-visibility", "children"),
     Output("weather-couldiness", "children"),
     Output("weather-min-temperature", "children"),
     Output("weather-max-temperature", "children")],
    [Input("location-dropdown", "value"),
     Input("date-dropdown", "value")]
)
def update_weather_info(selected_location, selected_date):
    if not selected_location or not selected_date:
        return "请选择站点和日期。", "", "", "", "", "", "", "", "", "", ""

    try:
        response = requests.get(f"http://localhost:8080/api/weather/{selected_location}/{selected_date}")
        if response.status_code == 200:
            weather_data = response.json()
            if "error" in weather_data:
                return weather_data["error"], "", "", "", "", "", "", "", "", "", ""

            title = f"天气信息 - {weather_data['stationId']} ({weather_data['date']})"
            temperature = f"温度: {weather_data.get('temperature', 'N/A')}"
            pression_ocean = f"海洋压力: {weather_data.get('pression_ocean', 'N/A')}"
            wind_direction = f"风向: {weather_data.get('wind_direction', 'N/A')}"
            wind_speed = f"风速: {weather_data.get('wind_speed', 'N/A')}"
            dew_point = f"露点: {weather_data.get('dew_point', 'N/A')}"
            humidity = f"湿度: {weather_data.get('humidity', 'N/A')}"
            horizontal_visibility = f"水平能见度: {weather_data.get('horizontal_visibility', 'N/A')}"
            couldiness = f"云量: {weather_data.get('couldiness', 'N/A')}"
            min_temperature = f"最低温度: {weather_data.get('min_temperature', 'N/A')}"
            max_temperature = f"最高温度: {weather_data.get('max_temperature', 'N/A')}"
            return (title, temperature, pression_ocean, wind_direction, wind_speed,
                    dew_point, humidity, horizontal_visibility, couldiness,
                    min_temperature, max_temperature)

        return "无法获取天气信息。", "", "", "", "", "", "", "", "", "", ""
    except Exception as e:
        print(f"Error fetching weather info: {e}")
        return "发生错误，无法获取天气信息。", "", "", "", "", "", "", "", "", "", ""


# 运行服务器
if __name__ == "__main__":
    app.run_server(debug=True)
