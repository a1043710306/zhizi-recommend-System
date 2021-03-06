package com.inveno.ser;
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ResponData1.proto

public final class ResponDataPro {
  private ResponDataPro() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ResponParamOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required string strategy = 1;
    /**
     * <code>required string strategy = 1;</code>
     */
    boolean hasStrategy();
    /**
     * <code>required string strategy = 1;</code>
     */
    java.lang.String getStrategy();
    /**
     * <code>required string strategy = 1;</code>
     */
    com.google.protobuf.ByteString
        getStrategyBytes();

    // required string infoid = 2;
    /**
     * <code>required string infoid = 2;</code>
     */
    boolean hasInfoid();
    /**
     * <code>required string infoid = 2;</code>
     */
    java.lang.String getInfoid();
    /**
     * <code>required string infoid = 2;</code>
     */
    com.google.protobuf.ByteString
        getInfoidBytes();

    // optional double gmp = 3;
    /**
     * <code>optional double gmp = 3;</code>
     */
    boolean hasGmp();
    /**
     * <code>optional double gmp = 3;</code>
     */
    double getGmp();

    // optional double adultScore = 4;
    /**
     * <code>optional double adultScore = 4;</code>
     */
    boolean hasAdultScore();
    /**
     * <code>optional double adultScore = 4;</code>
     */
    double getAdultScore();

    // optional int32 categoryId = 5;
    /**
     * <code>optional int32 categoryId = 5;</code>
     */
    boolean hasCategoryId();
    /**
     * <code>optional int32 categoryId = 5;</code>
     */
    int getCategoryId();

    // optional double keywordScores = 6;
    /**
     * <code>optional double keywordScores = 6;</code>
     */
    boolean hasKeywordScores();
    /**
     * <code>optional double keywordScores = 6;</code>
     */
    double getKeywordScores();

    // optional int32 imgCnt = 7;
    /**
     * <code>optional int32 imgCnt = 7;</code>
     */
    boolean hasImgCnt();
    /**
     * <code>optional int32 imgCnt = 7;</code>
     */
    int getImgCnt();
  }
  /**
   * Protobuf type {@code ResponParam}
   */
  public static final class ResponParam extends
      com.google.protobuf.GeneratedMessage
      implements ResponParamOrBuilder {
    // Use ResponParam.newBuilder() to construct.
    private ResponParam(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private ResponParam(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final ResponParam defaultInstance;
    public static ResponParam getDefaultInstance() {
      return defaultInstance;
    }

    public ResponParam getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private ResponParam(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              strategy_ = input.readBytes();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              infoid_ = input.readBytes();
              break;
            }
            case 25: {
              bitField0_ |= 0x00000004;
              gmp_ = input.readDouble();
              break;
            }
            case 33: {
              bitField0_ |= 0x00000008;
              adultScore_ = input.readDouble();
              break;
            }
            case 40: {
              bitField0_ |= 0x00000010;
              categoryId_ = input.readInt32();
              break;
            }
            case 49: {
              bitField0_ |= 0x00000020;
              keywordScores_ = input.readDouble();
              break;
            }
            case 56: {
              bitField0_ |= 0x00000040;
              imgCnt_ = input.readInt32();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ResponDataPro.internal_static_ResponParam_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ResponDataPro.internal_static_ResponParam_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ResponDataPro.ResponParam.class, ResponDataPro.ResponParam.Builder.class);
    }

    public static com.google.protobuf.Parser<ResponParam> PARSER =
        new com.google.protobuf.AbstractParser<ResponParam>() {
      public ResponParam parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new ResponParam(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<ResponParam> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required string strategy = 1;
    public static final int STRATEGY_FIELD_NUMBER = 1;
    private java.lang.Object strategy_;
    /**
     * <code>required string strategy = 1;</code>
     */
    public boolean hasStrategy() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string strategy = 1;</code>
     */
    public java.lang.String getStrategy() {
      java.lang.Object ref = strategy_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          strategy_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string strategy = 1;</code>
     */
    public com.google.protobuf.ByteString
        getStrategyBytes() {
      java.lang.Object ref = strategy_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        strategy_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    // required string infoid = 2;
    public static final int INFOID_FIELD_NUMBER = 2;
    private java.lang.Object infoid_;
    /**
     * <code>required string infoid = 2;</code>
     */
    public boolean hasInfoid() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required string infoid = 2;</code>
     */
    public java.lang.String getInfoid() {
      java.lang.Object ref = infoid_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          infoid_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string infoid = 2;</code>
     */
    public com.google.protobuf.ByteString
        getInfoidBytes() {
      java.lang.Object ref = infoid_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        infoid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    // optional double gmp = 3;
    public static final int GMP_FIELD_NUMBER = 3;
    private double gmp_;
    /**
     * <code>optional double gmp = 3;</code>
     */
    public boolean hasGmp() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional double gmp = 3;</code>
     */
    public double getGmp() {
      return gmp_;
    }

    // optional double adultScore = 4;
    public static final int ADULTSCORE_FIELD_NUMBER = 4;
    private double adultScore_;
    /**
     * <code>optional double adultScore = 4;</code>
     */
    public boolean hasAdultScore() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>optional double adultScore = 4;</code>
     */
    public double getAdultScore() {
      return adultScore_;
    }

    // optional int32 categoryId = 5;
    public static final int CATEGORYID_FIELD_NUMBER = 5;
    private int categoryId_;
    /**
     * <code>optional int32 categoryId = 5;</code>
     */
    public boolean hasCategoryId() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    /**
     * <code>optional int32 categoryId = 5;</code>
     */
    public int getCategoryId() {
      return categoryId_;
    }

    // optional double keywordScores = 6;
    public static final int KEYWORDSCORES_FIELD_NUMBER = 6;
    private double keywordScores_;
    /**
     * <code>optional double keywordScores = 6;</code>
     */
    public boolean hasKeywordScores() {
      return ((bitField0_ & 0x00000020) == 0x00000020);
    }
    /**
     * <code>optional double keywordScores = 6;</code>
     */
    public double getKeywordScores() {
      return keywordScores_;
    }

    // optional int32 imgCnt = 7;
    public static final int IMGCNT_FIELD_NUMBER = 7;
    private int imgCnt_;
    /**
     * <code>optional int32 imgCnt = 7;</code>
     */
    public boolean hasImgCnt() {
      return ((bitField0_ & 0x00000040) == 0x00000040);
    }
    /**
     * <code>optional int32 imgCnt = 7;</code>
     */
    public int getImgCnt() {
      return imgCnt_;
    }

    private void initFields() {
      strategy_ = "";
      infoid_ = "";
      gmp_ = 0D;
      adultScore_ = 0D;
      categoryId_ = 0;
      keywordScores_ = 0D;
      imgCnt_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasStrategy()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasInfoid()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getStrategyBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getInfoidBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeDouble(3, gmp_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeDouble(4, adultScore_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeInt32(5, categoryId_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        output.writeDouble(6, keywordScores_);
      }
      if (((bitField0_ & 0x00000040) == 0x00000040)) {
        output.writeInt32(7, imgCnt_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getStrategyBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getInfoidBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(3, gmp_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(4, adultScore_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(5, categoryId_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(6, keywordScores_);
      }
      if (((bitField0_ & 0x00000040) == 0x00000040)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(7, imgCnt_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static ResponDataPro.ResponParam parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ResponDataPro.ResponParam parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ResponDataPro.ResponParam parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ResponDataPro.ResponParam parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ResponDataPro.ResponParam parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ResponDataPro.ResponParam parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static ResponDataPro.ResponParam parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static ResponDataPro.ResponParam parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static ResponDataPro.ResponParam parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ResponDataPro.ResponParam parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(ResponDataPro.ResponParam prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code ResponParam}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements ResponDataPro.ResponParamOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ResponDataPro.internal_static_ResponParam_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ResponDataPro.internal_static_ResponParam_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ResponDataPro.ResponParam.class, ResponDataPro.ResponParam.Builder.class);
      }

      // Construct using ResponData1.ResponParam.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        strategy_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        infoid_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        gmp_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000004);
        adultScore_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000008);
        categoryId_ = 0;
        bitField0_ = (bitField0_ & ~0x00000010);
        keywordScores_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000020);
        imgCnt_ = 0;
        bitField0_ = (bitField0_ & ~0x00000040);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ResponDataPro.internal_static_ResponParam_descriptor;
      }

      public ResponDataPro.ResponParam getDefaultInstanceForType() {
        return ResponDataPro.ResponParam.getDefaultInstance();
      }

      public ResponDataPro.ResponParam build() {
        ResponDataPro.ResponParam result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public ResponDataPro.ResponParam buildPartial() {
        ResponDataPro.ResponParam result = new ResponDataPro.ResponParam(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.strategy_ = strategy_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.infoid_ = infoid_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.gmp_ = gmp_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.adultScore_ = adultScore_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.categoryId_ = categoryId_;
        if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
          to_bitField0_ |= 0x00000020;
        }
        result.keywordScores_ = keywordScores_;
        if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
          to_bitField0_ |= 0x00000040;
        }
        result.imgCnt_ = imgCnt_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof ResponDataPro.ResponParam) {
          return mergeFrom((ResponDataPro.ResponParam)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ResponDataPro.ResponParam other) {
        if (other == ResponDataPro.ResponParam.getDefaultInstance()) return this;
        if (other.hasStrategy()) {
          bitField0_ |= 0x00000001;
          strategy_ = other.strategy_;
          onChanged();
        }
        if (other.hasInfoid()) {
          bitField0_ |= 0x00000002;
          infoid_ = other.infoid_;
          onChanged();
        }
        if (other.hasGmp()) {
          setGmp(other.getGmp());
        }
        if (other.hasAdultScore()) {
          setAdultScore(other.getAdultScore());
        }
        if (other.hasCategoryId()) {
          setCategoryId(other.getCategoryId());
        }
        if (other.hasKeywordScores()) {
          setKeywordScores(other.getKeywordScores());
        }
        if (other.hasImgCnt()) {
          setImgCnt(other.getImgCnt());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasStrategy()) {
          
          return false;
        }
        if (!hasInfoid()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        ResponDataPro.ResponParam parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (ResponDataPro.ResponParam) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required string strategy = 1;
      private java.lang.Object strategy_ = "";
      /**
       * <code>required string strategy = 1;</code>
       */
      public boolean hasStrategy() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string strategy = 1;</code>
       */
      public java.lang.String getStrategy() {
        java.lang.Object ref = strategy_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          strategy_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string strategy = 1;</code>
       */
      public com.google.protobuf.ByteString
          getStrategyBytes() {
        java.lang.Object ref = strategy_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          strategy_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string strategy = 1;</code>
       */
      public Builder setStrategy(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        strategy_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string strategy = 1;</code>
       */
      public Builder clearStrategy() {
        bitField0_ = (bitField0_ & ~0x00000001);
        strategy_ = getDefaultInstance().getStrategy();
        onChanged();
        return this;
      }
      /**
       * <code>required string strategy = 1;</code>
       */
      public Builder setStrategyBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        strategy_ = value;
        onChanged();
        return this;
      }

      // required string infoid = 2;
      private java.lang.Object infoid_ = "";
      /**
       * <code>required string infoid = 2;</code>
       */
      public boolean hasInfoid() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required string infoid = 2;</code>
       */
      public java.lang.String getInfoid() {
        java.lang.Object ref = infoid_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          infoid_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string infoid = 2;</code>
       */
      public com.google.protobuf.ByteString
          getInfoidBytes() {
        java.lang.Object ref = infoid_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          infoid_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string infoid = 2;</code>
       */
      public Builder setInfoid(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        infoid_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string infoid = 2;</code>
       */
      public Builder clearInfoid() {
        bitField0_ = (bitField0_ & ~0x00000002);
        infoid_ = getDefaultInstance().getInfoid();
        onChanged();
        return this;
      }
      /**
       * <code>required string infoid = 2;</code>
       */
      public Builder setInfoidBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        infoid_ = value;
        onChanged();
        return this;
      }

      // optional double gmp = 3;
      private double gmp_ ;
      /**
       * <code>optional double gmp = 3;</code>
       */
      public boolean hasGmp() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional double gmp = 3;</code>
       */
      public double getGmp() {
        return gmp_;
      }
      /**
       * <code>optional double gmp = 3;</code>
       */
      public Builder setGmp(double value) {
        bitField0_ |= 0x00000004;
        gmp_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double gmp = 3;</code>
       */
      public Builder clearGmp() {
        bitField0_ = (bitField0_ & ~0x00000004);
        gmp_ = 0D;
        onChanged();
        return this;
      }

      // optional double adultScore = 4;
      private double adultScore_ ;
      /**
       * <code>optional double adultScore = 4;</code>
       */
      public boolean hasAdultScore() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>optional double adultScore = 4;</code>
       */
      public double getAdultScore() {
        return adultScore_;
      }
      /**
       * <code>optional double adultScore = 4;</code>
       */
      public Builder setAdultScore(double value) {
        bitField0_ |= 0x00000008;
        adultScore_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double adultScore = 4;</code>
       */
      public Builder clearAdultScore() {
        bitField0_ = (bitField0_ & ~0x00000008);
        adultScore_ = 0D;
        onChanged();
        return this;
      }

      // optional int32 categoryId = 5;
      private int categoryId_ ;
      /**
       * <code>optional int32 categoryId = 5;</code>
       */
      public boolean hasCategoryId() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      /**
       * <code>optional int32 categoryId = 5;</code>
       */
      public int getCategoryId() {
        return categoryId_;
      }
      /**
       * <code>optional int32 categoryId = 5;</code>
       */
      public Builder setCategoryId(int value) {
        bitField0_ |= 0x00000010;
        categoryId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int32 categoryId = 5;</code>
       */
      public Builder clearCategoryId() {
        bitField0_ = (bitField0_ & ~0x00000010);
        categoryId_ = 0;
        onChanged();
        return this;
      }

      // optional double keywordScores = 6;
      private double keywordScores_ ;
      /**
       * <code>optional double keywordScores = 6;</code>
       */
      public boolean hasKeywordScores() {
        return ((bitField0_ & 0x00000020) == 0x00000020);
      }
      /**
       * <code>optional double keywordScores = 6;</code>
       */
      public double getKeywordScores() {
        return keywordScores_;
      }
      /**
       * <code>optional double keywordScores = 6;</code>
       */
      public Builder setKeywordScores(double value) {
        bitField0_ |= 0x00000020;
        keywordScores_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double keywordScores = 6;</code>
       */
      public Builder clearKeywordScores() {
        bitField0_ = (bitField0_ & ~0x00000020);
        keywordScores_ = 0D;
        onChanged();
        return this;
      }

      // optional int32 imgCnt = 7;
      private int imgCnt_ ;
      /**
       * <code>optional int32 imgCnt = 7;</code>
       */
      public boolean hasImgCnt() {
        return ((bitField0_ & 0x00000040) == 0x00000040);
      }
      /**
       * <code>optional int32 imgCnt = 7;</code>
       */
      public int getImgCnt() {
        return imgCnt_;
      }
      /**
       * <code>optional int32 imgCnt = 7;</code>
       */
      public Builder setImgCnt(int value) {
        bitField0_ |= 0x00000040;
        imgCnt_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int32 imgCnt = 7;</code>
       */
      public Builder clearImgCnt() {
        bitField0_ = (bitField0_ & ~0x00000040);
        imgCnt_ = 0;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:ResponParam)
    }

    static {
      defaultInstance = new ResponParam(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:ResponParam)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_ResponParam_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_ResponParam_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\021ResponData1.proto\"\213\001\n\013ResponParam\022\020\n\010s" +
      "trategy\030\001 \002(\t\022\016\n\006infoid\030\002 \002(\t\022\013\n\003gmp\030\003 \001" +
      "(\001\022\022\n\nadultScore\030\004 \001(\001\022\022\n\ncategoryId\030\005 \001" +
      "(\005\022\025\n\rkeywordScores\030\006 \001(\001\022\016\n\006imgCnt\030\007 \001(" +
      "\005"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_ResponParam_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_ResponParam_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_ResponParam_descriptor,
              new java.lang.String[] { "Strategy", "Infoid", "Gmp", "AdultScore", "CategoryId", "KeywordScores", "ImgCnt", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
