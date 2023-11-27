from flask import Flask, render_template, request, jsonify

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/file_load', methods=['POST'])
def file_load():
    try:
        file = request.files['fileInput']
        json_data = file.read().decode('utf-8')
        return jsonify({"success": True, "data": json_data})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)})

if __name__ == '__main__':
    app.run(debug=True)
