# DeviceConnectApp

NTTドコモが開発した「Device Connect」で使用するデバイスプラグインの動作検証を行うためのアプリです。

## 概要

NTTドコモが開発した「Device Connect」は、スマートフォン上で動作する仮想サーバーに対してRESTによるリクエストを送信することで、様々なウェアラブルデバイスやIoTデバイスをWebブラウザやアプリから統一的な記述で簡単に利用することができます。

この「DeviceConnectApp」は、「Device Connect」で使用するデバイスプラグインの機能をXMLファイルに記載し、そのファイルを元に動作確認用の画面を自動で生成し、リクエストの送信及びレスポンスの確認を行うことができるアプリです。

## 動作要件

* dConnectSDKFroAndroidモジュールをインポートしてください(2015/02/18版対応)。
 * https://github.com/DeviceConnect

* 利用するAndroid端末に「Device Connect Manager」をインストールしてください。

## XMLファイル

デバイスプラグインの情報を定義するXMLファイルは、以下の書式で定義し、「デバイスプラグイン名.xml」として保存します。
サンプルは本プロジェクトのassetフォルダを参照してください。

    <?xml version="1.0" encoding="utf-8"?>
    <DeviceConnectParam>`
        <Profile name="Profile">
            <!-- プロファイルの持つ機能を定義 -->
            <Attribute>
                <Name>表示名</Name>
                <Path>Interface/Attribute</Path>
                <Method>GET/PUT/POST/DELETE/EVENT</Method>
                <!-- オプションがある場合 -->
                <Option>
                    <Name>オプション名</Name>
                    <!-- 
                        オプション入力欄の形式
                        text : テキストボックス
                        select : ドロップダウンリスト形式
                        file : ファイルパス選択
                    -->
                    <Type>text/select/file</Type>
                    <!-- 入力欄にselectを指定した場合の選択肢を定義 -->
                    <Value>foo</Value>
                    <Value>bar</Value>
                </Option>
            </Attribute>
        </Profile>
    </DeviceConnectParam>


## 準備

1. デバイスプラグインの情報を記載したXMLファイルを準備します。
2. XMLファイルを任意のサーバーにアップロードします。
3. アプリを起動し、Menu > Settings > XML Sourceを「Web」に切り替え、サーバーのURLを入力します。

## 使用方法

1. [Device Discovery]ボタンをクリックして、インストールされているデバイスプラグインを取得します。
2. 操作したいデバイスプラグイン名をクリックします。
3. テストするプラグイン名をクリックします。
4. 必要なオプションを入力し、テストする機能のボタンをクリックします。

## 権限について

プラグイン名をクリックした際に「Request is out of scope」と表示される場合は、Menu > Permissionsを開き、必要な権限を登録してください。
