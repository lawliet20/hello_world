package com.wwj.testDemo;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.jsums.jsums_app.DumpUtils;
import com.jsums.jsums_app.ExecutMessage;

public class Test {

	public static String enCode() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {

		String privateKeyStr = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK+1uQPdtQEzNbf4sq0zNij/M4qI/hRoQjI5iTTOxJKMY6a32N4nLFlmoVV3wQT2+PUnOybtyZwyjEv8j3QT89PixbvbAHRD6KZIbkEMBl5spOXWiCyjwy/7m5nhM9COMu3DO3bF2ouqQqZm9dYf1AYvLWuvU+zwS7TgmxRw6XyrAgMBAAECgYA+CZhIUCgPQ2htCyby1gkCUpB9Ej87L1Bn8T6LYZGv+FdazsCINyaGbiD6TyzcNuLRk8djyEMNnh9A2OxBXKYSjCB2KsX3492KibAi4vSlz2N7VX74nGVPNRcerPLeWI2zj+FtyF3+tOyI59I5byajk6y5vjm8gE7U/n0FFRsHIQJBAOmbbXq80ldsUH39qLft7R1pocIFwIR4fkiPnXentZ/MAl/YJfjKjCW+x2TlUaVkfXGvV1zBEbuTfXU4mkEHUFsCQQDAjYg2uuP2zaAzq0fq3SqlZDFzBa3snxo+6Kx0gVkgQQOwPycMouCVtIqqoK8fyoB45R7xshpSuGa2c2udbDXxAkEApjWdFnfkXKlHN+1TrtINJCE1Ixv+lwI4L55nFmxv8GsxwUnD8pkCUBTGP8ZdagTusAVmbis8V66f09hbACuZAwJAbUOjCROSaqmSf99xBviLy6CIYvHVGRHLrekQe2gu8BuUgT3E5enDMZBqo5pc5dhegLvbjGF5iHk1Z9ElPwZ5sQJAGw7cAY1HR10ue1l6gKrEtYVZ2Jz07XqOY87ip6ocI6nUyx8R7yC9P1YHL/OH3ibWmyBTkc65UZAIaVpDA0afKw==";
		String publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvtbkD3bUBMzW3+LKtMzYo/zOKiP4UaEIyOYk0zsSSjGOmt9jeJyxZZqFVd8EE9vj1Jzsm7cmcMoxL/I90E/PT4sW72wB0Q+imSG5BDAZebKTl1ogso8Mv+5uZ4TPQjjLtwzt2xdqLqkKmZvXWH9QGLy1rr1Ps8Eu04JsUcOl8qwIDAQAB";

		//publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1OyWe5qgUf3tG08fGMs6Q4MZ+rrJCqVFh/MCUalkZFGXzI8Jpt3SpmxRxjKdWj2kmClnNQPUEcSCxjo80l8eIegrKduQuXpaxLI8iyYvZEZEyBWmilNP++uUnCsGN2OXwjokVzSM8sRVw3tCoQvKAW+uSjDlXaxArvTjBOiiSAQIDAQAB";
		// String privateKeyStr = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMCcinDH5WI0Rv3YrikDqmrYGd/z6QSp7tEhTnFAJWgtVCpV0twm0J2fPZboJeD/Hv3FrgH9jInphTLIvXoDVgg2di7rChMSOISNq9bs5c6jBNlSCo+aAJAriD+5Yh8WbQNQQtDsxU0nK5KzsuCKV6uOFG4fF1f6MNZsQ/hgXLKTAgMBAAECgYBQHQxsKojbVvksPoL6Jj8s36Orhe0m9nOR4rOY4WBtu2Tlkvr4fOCKnyBj8z1GH3dJgH6G72oUNuAjJmHBLUALw7bE6SUW2tXN3Wt96/uvQhTSnSd6FypwbT8fCducH1TNr/3PePWK75zPc6FRV28jvKv7/E43THGJ9j2SmJYTiQJBAODDHCs5Ea5J6v4bsstUTbCwWfywtN6SG5Zlo9Vemd7TzmhTUbTJ6UjxmeCY1CSue6EPy7oNkpm9/aMggVvk8FcCQQDbYYgC0vMPTzPZksIUnKiNY/79pz8hJ3EGsz49aeo44FsORqKlKouXmwWjxXN16bHxK/WXHt4nic4SevToV/olAkEAl2aWaP8uS8r7AmTCEXkeRDDmzPJzQ6ID91FMBQSOfa0LUvcdCL0h6cLlNod8D6GBIcM0JoXSBMIYQnQ47x/OTQJABmTDSAHJ36pZQoAKc/tU1joR1manalnx61YR/Ew37Uxsmu/oEZQ52UNFWM6KXOdgrjyvvaXCp7hWbydyN8tImQJAbKaHxHIbkR4gr0Jk6wpWJTFMbc7S1U1PeMM4XYwKcHITLzTirWd+1Lj88E5kpAOfeV/KMpNc65QRnMGeE6vPXA==";
		// String publicKeyStr= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDAnIpwx+ViNEb92K4pA6pq2Bnf8+kEqe7RIU5xQCVoLVQqVdLcJtCdnz2W6CXg/x79xa4B/YyJ6YUyyL16A1YINnYu6woTEjiEjavW7OXOowTZUgqPmgCQK4g/uWIfFm0DUELQ7MVNJyuSs7LgilerjhRuHxdX+jDWbEP4YFyykwIDAQAB";

		String messageString = "{\"extOrderNo\":\"666666666987611\",\"merAbbr\":\"NJCKH\",\"mrchNo\":\"898320159983203\",\"appAccessKeyId\":\"7ce603d9f22843e4b685eaa4015067bb\",\"orderAmt\":0.1,\"cardHolderNo\":\"clj\"}\"";// 加密数据
		// messageString="ss";
		messageString = "";
		String messagess = ExecutMessage.encode(messageString, privateKeyStr);// 数据签名
		System.out.println("签名后的数据为====" + messagess);

		messagess = ExecutMessage.encode(messageString, privateKeyStr);
		String messageString2 = "MTU5OTkwQTBDNUM2NDVBODQ3ODkyQzAyMTE1QkY2Mzc0RjRDOTJCMjMyNjQ5RUJEQTY0ODczQTlFRTIxM0EwRTUzMjBGNDIxOTVBNzVDMUU4QUE4N0RBMDYxNEU1RjI0NDI0RUYzNDlFNzRGQjRFQjlDRDkyODI3NTlCNUZCNkZBRDNFRDdBREVDNzQ0NkNBMDM2ODVGMTk3OERGMjJBMDE1OUVBQ0JDMDY4NTRFOUQ4RjFGOTVCOUY0RENFQzlDMEZGN0I0QkE0NjU3OTcwRDgyQTA0OEI3OTA2Q0VDRTczNjMyODRFOURDRTQ5QTFBODFBMERBRjY3MDBFMTEyOA";
		// 待验签的数据
		boolean isOk = ExecutMessage.deCode(messageString, messagess, publicKeyStr);// 验证签名

		System.out.println("isok==++++" + isOk);
		String messString64 = ExecutMessage.enCode64(messageString);// base64加密
		String messStringBackString = ExecutMessage.deCode64(messString64);// base64解密
		return null;
	}

	// 上传参数
	public void encode() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		String privateKeyStr = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMCcinDH5WI0Rv3YrikDqmrYGd/z6QSp7tEhTnFAJWgtVCpV0twm0J2fPZboJeD/Hv3FrgH9jInphTLIvXoDVgg2di7rChMSOISNq9bs5c6jBNlSCo+aAJAriD+5Yh8WbQNQQtDsxU0nK5KzsuCKV6uOFG4fF1f6MNZsQ/hgXLKTAgMBAAECgYBQHQxsKojbVvksPoL6Jj8s36Orhe0m9nOR4rOY4WBtu2Tlkvr4fOCKnyBj8z1GH3dJgH6G72oUNuAjJmHBLUALw7bE6SUW2tXN3Wt96/uvQhTSnSd6FypwbT8fCducH1TNr/3PePWK75zPc6FRV28jvKv7/E43THGJ9j2SmJYTiQJBAODDHCs5Ea5J6v4bsstUTbCwWfywtN6SG5Zlo9Vemd7TzmhTUbTJ6UjxmeCY1CSue6EPy7oNkpm9/aMggVvk8FcCQQDbYYgC0vMPTzPZksIUnKiNY/79pz8hJ3EGsz49aeo44FsORqKlKouXmwWjxXN16bHxK/WXHt4nic4SevToV/olAkEAl2aWaP8uS8r7AmTCEXkeRDDmzPJzQ6ID91FMBQSOfa0LUvcdCL0h6cLlNod8D6GBIcM0JoXSBMIYQnQ47x/OTQJABmTDSAHJ36pZQoAKc/tU1joR1manalnx61YR/Ew37Uxsmu/oEZQ52UNFWM6KXOdgrjyvvaXCp7hWbydyN8tImQJAbKaHxHIbkR4gr0Jk6wpWJTFMbc7S1U1PeMM4XYwKcHITLzTirWd+1Lj88E5kpAOfeV/KMpNc65QRnMGeE6vPXA==";

		String messageString = "{\"extOrderNo\":\"666666666987611\",\"merAbbr\":\"NJCKH\",\"mrchNo\":\"898320159983203\",\"appAccessKeyId\":\"7ce603d9f22843e4b685eaa4015067bb\",\"orderAmt\":0.1,\"cardHolderNo\":\"clj\"}\"";// 加密数据
		messageString = "";
		String signData = ExecutMessage.encode(messageString, privateKeyStr);// 数据签名
		System.out.println("签名后的数据为====" + signData);

		String reqOrd = ExecutMessage.enCode64(messageString);// base64加密

	}

	// 验证参数
	public static void decode() {
		String publicString = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1OyWe5qgUf3tG08fGMs6Q4MZ+rrJCqVFh/MCUalkZFGXzI8Jpt3SpmxRxjKdWj2kmClnNQPUEcSCxjo80l8eIegrKduQuXpaxLI8iyYvZEZEyBWmilNP++uUnCsGN2OXwjokVzSM8sRVw3tCoQvKAW+uSjDlXaxArvTjBOiiSAQIDAQAB";
		String messageString = "amount=0.10&currency=156&extOrderNo=6666666669876&mrchName=UPMP有卡测试商户&mrchNo=898320159983203&orderNo=1450340502938000055&orderStatus=6&orderTime=2015-12-16 10:59:57";
		String signDataString = "rLB3xXsRp90OmtRgJdi3-SMXWIYQCO7DRG_xRwGjj_EnNZj9NLbRuWYMG_HxC3V2qG_HaGmoEF3j8vKYG23d6X-YG81W_yln9ggwl5jXNpRQWj80-JCKBmyctHGk_GhO1btOj0l_Swc6YduAdC6W3lMWKx5pDW3ZBdzzmN4OveQ";
		boolean isOk = ExecutMessage.deCode(messageString, signDataString, publicString);
		System.out.println(isOk);
	}

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		// TODO Auto-generated method stub
		// enCode();
		 
		//decode();

	}

}
