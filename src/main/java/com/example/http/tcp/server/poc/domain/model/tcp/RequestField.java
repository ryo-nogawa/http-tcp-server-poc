package com.example.http.tcp.server.poc.domain.model.tcp;

/**
 * TCPリクエスト電文のフィールド定義.
 *
 * <p>各フィールドはオフセット・長さ・属性を保持し、
 * バイト配列への書き込み・読み込みを行う。
 */
public enum RequestField {

    /**
     * データ長フィールド.
     *
     * <p>オフセット0、長さ4バイト、属性はTXT。
     */
    DATA_LENGTH(0, 4, FieldType.TXT),

    /**
     * 電文種別フィールド.
     *
     * <p>オフセット4、長さ2バイト、属性はBCD。
     */
    MESSAGE_TYPE(4, 2, FieldType.BCD),

    /**
     * カード番号フィールド.
     *
     * <p>オフセット6、長さ8バイト、属性はBCD。
     */
    CARD_NUMBER(6, 8, FieldType.BCD),

    /**
     * 金額フィールド.
     *
     * <p>オフセット14、長さ4バイト、属性はHEX。
     */
    AMOUNT(14, 4, FieldType.HEX);

    private final int offset;
    private final int length;
    private final FieldType fieldType;

    /**
     * コンストラクタ.
     *
     * @param offset フィールドのオフセット
     * @param length フィールドの長さ
     * @param fieldType フィールドの属性
     */
    RequestField(int offset, int length, FieldType fieldType) {
        this.offset = offset;
        this.length = length;
        this.fieldType = fieldType;
    }

    /**
     * フィールドのオフセットを返す.
     *
     * @return オフセット
     */
    public int getOffset() {
        return offset;
    }

    /**
     * バイト配列から自身の範囲を切り出して変換する.
     *
     * <p>バイト配列の{@code offset}から{@code length}バイト分を
     * 抽出し、自身の{@code fieldType}でデコードして返す。
     *
     * @param data デコード対象のバイト配列
     * @return デコード済みの文字列
     * @throws TcpMessageFormatException デコード失敗時
     */
    public String extract(byte[] data) {
        byte[] fieldData = new byte[length];
        System.arraycopy(data, offset, fieldData, 0, length);
        return fieldType.decode(fieldData);
    }

    /**
     * バイト配列の自身の範囲へ書き込む.
     *
     * <p>指定の文字列を自身の{@code fieldType}でエンコードし、
     * バイト配列の{@code offset}から{@code length}バイト分に書き込む。
     *
     * @param data 書き込み対象のバイト配列
     * @param value 書き込む文字列
     * @throws TcpMessageFormatException エンコード失敗時
     */
    public void write(byte[] data, String value) {
        byte[] encoded = fieldType.encode(value, length);
        System.arraycopy(encoded, 0, data, offset, length);
    }

    /**
     * 全リクエストフィールドの合計バイト数を返す.
     *
     * @return 合計バイト数
     */
    public static int totalLength() {
        return 18;
    }

}
