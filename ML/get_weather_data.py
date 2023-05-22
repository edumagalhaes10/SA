import requests
import json
import re
from datetime import date

def split_array(array, chunk_size):
    return [array[i:i+chunk_size] for i in range(0, len(array), chunk_size)]

current_date = date.today()
print(current_date)
url = f"https://archive-api.open-meteo.com/v1/archive?latitude=41.55&longitude=-8.42&start_date=2020-01-01&end_date={current_date}&hourly=temperature_2m"

headers = {"Accept": "application/json"}

print("Efetuado um pedido HTTP...")
response = requests.get(url, headers=headers)
with open("response.json", "wb") as f:
    f.write(response.content)

with open('response.json','r') as f:
    content = json.load(f)

    # print(content["hourly"]["temperature_2m"])
group_24by24_temperature = []
group_24by24_temperature = split_array(content["hourly"]["temperature_2m"], 24)
# print(group_24by24_temperature)
group_24by24_time = []
group_24by24_time = split_array(content["hourly"]["time"], 24)
# print(group_24by24_time)

csv_string = "time,temperature\n"
for x in range(len(group_24by24_temperature)):
    for y in range(24):
        new_time = re.sub("T", " ", group_24by24_time[x][y])
        new_temp = group_24by24_temperature[x][y] 
        if new_temp == None:
            new_temp = "NaN"
        csv_string += f"{new_time},{new_temp}\n"


with open("weather_data.csv", "w") as f:
    f.write(csv_string)
    print(csv_string)
