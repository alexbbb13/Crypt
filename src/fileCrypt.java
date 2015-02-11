import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class fileCrypt {
	private InputStream streamIn;
	private OutputStream streamOut;
	private int currentBlockLength = 0;
	private final static int BUF_LEN = 8192;
	private final int ARG_LEN = 8;
	private byte[] inBuf = new byte[BUF_LEN];
	private byte[] outBuf = new byte[BUF_LEN];
	private byte[] password = new byte[ARG_LEN];

	/**
	 * @param args
	 */

	/**
	 * "Криптографическое преобразование" данных на пароле password.
	 * Зашифровывает данные (data), если переданы открытые данные. И
	 * расшифровывает, если переданы зашифрованные данные. Результат - в result.
	 * 
	 * @param password
	 *            - пароль в виде массива байтов [len = 8].
	 * @param data
	 *            - открытые / зашифрованные данные [len = 8].
	 * @param result
	 *            - зашифрованные / открытые данные [len = 8].
	 */
	public static void ProcessData(final byte[] password, final byte[] data,
			byte[] result) {
		final int ARG_LEN = 8;
		if ((data.length != ARG_LEN) || (password.length != ARG_LEN)
				|| (result.length != ARG_LEN)) {
			throw new RuntimeException("Wrong len");
		}

		for (int i = 0; i < 8; ++i) {
			result[i] = (byte) (data[i] ^ password[i]);
		}
	}

	private void createStreams(String filenameIn, String filenameOut) {
		try {
			streamIn = new BufferedInputStream(new FileInputStream(filenameIn),
					BUF_LEN);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {

			streamOut = new BufferedOutputStream(new FileOutputStream(
					filenameOut), BUF_LEN);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void encryptBlock() {
		byte[] in = new byte[ARG_LEN];
		byte[] out = new byte[ARG_LEN];

		for (int i = 0; i < BUF_LEN; i += ARG_LEN) {
			for (int j = 0; j < ARG_LEN; j++)
				in[j] = inBuf[i + j];
			ProcessData(password, in, out);
			for (int j = 0; j < ARG_LEN; j++)
				outBuf[i + j] = out[j];
		}
		// тут может случиться небольшая дыра, т.к. последний блок криптуется
		// целиком, а т.к. хвост его забит нулями (обычно) то соответственно
		// получаем голый пароль на выходе(в пямяти). Правда конец блока все
		// равно в файл не пишется, и пароль ещё лежит в password, и ещё много
		// где, то пусть будет так.
	}

	private int readNextBlock() {
		try {
			currentBlockLength = streamIn.read(inBuf, 0, BUF_LEN);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return currentBlockLength;
	}

	private void writeBlock() {
		try {
			streamOut.write(outBuf, 0, currentBlockLength);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeAll() {
		try {
			streamIn.close();
			streamOut.flush();
			streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setPassword(String p) {
		password = p.getBytes();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if ((args.length < 4)) {
			throw new RuntimeException(
					"Usage: fileCrypt file1 file2 encrypt/decrypt password");
		}
		if (!((args[2].matches("encrypt")) || (args[2].matches("decrypt")))) {
			throw new RuntimeException(
					"Usage: fileCrypt file1 file2 encrypt/decrypt password");
		}
		if ((args[3].length() != 8)) {
			throw new RuntimeException("Password length must be 8");
		}

		fileCrypt crypt = new fileCrypt();
		crypt.setPassword(args[3]);
		// XOR криптовка/декриптовка одинакова поэтому аргумент [2] не проверяем
		crypt.createStreams(args[0], args[1]);
		while (crypt.readNextBlock() != -1) {
			crypt.encryptBlock();
			crypt.writeBlock();
		}
		crypt.closeAll();

	}

}
