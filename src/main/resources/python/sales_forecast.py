#!/usr/bin/env python

import sys
import traceback
import json
import pandas as pd
import matplotlib

matplotlib.use('Agg')  # Headless 환경(서버)에서 GUI 백엔드 문제 없게 설정

# 한글 폰트 설정 개선
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm
from matplotlib import rcParams

# NanumGothic 폰트 설정
# 기본 폰트 경로 설정 시도
try:
    font_path = fm.findfont('NanumGothic')
    if font_path:
        prop = fm.FontProperties(fname=font_path)
        plt.rcParams['font.family'] = 'NanumGothic'
        plt.rcParams['axes.unicode_minus'] = False  # 마이너스 기호 깨짐 방지
    else:
        # 폰트를 찾을 수 없는 경우 경고 출력
        print("Warning: NanumGothic font not found, using fallback font", file=sys.stderr)
        plt.rcParams['font.family'] = 'sans-serif'
except Exception as e:
    print(f"Font setting error: {str(e)}", file=sys.stderr)
    plt.rcParams['font.family'] = 'sans-serif'

from io import BytesIO
import base64


def generate_daily_sales_plot(df, unit="원", StrMethodFormatter=None):
    daily_sales = df['totalSales'].resample('D').sum()
    plt.figure(figsize=(12, 6))
    plt.plot(daily_sales.index, daily_sales.values, marker='o', linestyle='-')
    plt.xlabel("Date")
    plt.ylabel(f"Total Sales ({unit})")
    plt.title("날짜 별 판매 추세", fontsize=16)
    plt.grid()
    from matplotlib.ticker import StrMethodFormatter
    plt.gca().yaxis.set_major_formatter(StrMethodFormatter('{x:,.0f} ' + unit))

    # 그래프 여백 조정
    plt.tight_layout()

    buffer = BytesIO()
    plt.savefig(buffer, format="png", dpi=100)
    plt.close()
    buffer.seek(0)
    return base64.b64encode(buffer.read()).decode("utf-8")


def generate_hourly_sales_plot(df, unit="원", selected_date=None):
    df['hour'] = df.index.hour
    hourly_sales = df.groupby('hour')['totalSales'].sum()

    plt.figure(figsize=(12, 6))
    plt.plot(hourly_sales.index, hourly_sales.values, marker='o', linestyle='-')
    plt.xlabel("Hour of the Day")
    plt.ylabel(f"Total Sales ({unit})")
    plt.title("시간 별 판매 추세", fontsize=16)
    plt.xticks(range(0, 24))
    plt.grid()
    from matplotlib.ticker import StrMethodFormatter
    plt.gca().yaxis.set_major_formatter(StrMethodFormatter('{x:,.0f} ' + unit))

    # 그래프 여백 조정
    plt.tight_layout()

    buffer = BytesIO()
    plt.savefig(buffer, format="png", dpi=100)
    plt.close()
    buffer.seek(0)
    return base64.b64encode(buffer.read()).decode("utf-8")


def main():
    try:
        # stdin 전체 읽기 (타임아웃 없음)
        raw_input = sys.stdin.read().strip()
        if not raw_input:
            raise ValueError("No input data received")

        # JSON 파싱
        json_obj = json.loads(raw_input)

        # salesData & selectedDate 파악
        if isinstance(json_obj, dict) and "salesData" in json_obj:
            sales_data = json_obj["salesData"]
            selected_date = json_obj.get("selectedDate", None)
        elif isinstance(json_obj, list):
            sales_data = json_obj
            selected_date = None
        else:
            raise ValueError("Input JSON must be a dict with 'salesData' or a list")

        df = pd.DataFrame(sales_data)
        if "date" not in df.columns or "totalSales" not in df.columns:
            raise ValueError("JSON must contain 'date' and 'totalSales' fields")

        df['date'] = pd.to_datetime(df['date'])
        df.set_index('date', inplace=True)

        unit = "원"
        daily_b64 = generate_daily_sales_plot(df, unit)
        hourly_b64 = generate_hourly_sales_plot(df, unit, selected_date)

        result = {
            "forecast_message": f"Hourly graph generated for {selected_date}" if selected_date else "Hourly graph generated (all dates)",
            "daily_image_base64": daily_b64,
            "hourly_image_base64": hourly_b64,
            "sales_unit": unit
        }

        # stdout으로 결과 JSON 출력
        print(json.dumps(result, ensure_ascii=False))
        sys.stdout.flush()

    except Exception as e:
        error_info = {
            "error": str(e),
            "trace": traceback.format_exc()
        }
        print(json.dumps(error_info, ensure_ascii=False))
        sys.stdout.flush()
        sys.exit(1)


if __name__ == "__main__":
    main()
