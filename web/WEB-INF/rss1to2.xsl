<?xml version="1.0" encoding="UTF-8"?><!-- DWXMLSource="entertainment.xml" -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/> 
	<xsl:template match="/">
		
		<rdf:RDF
			xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			xmlns="http://purl.org/rss/1.0/"
		>
		<!-- Parse Channel-->
		<xsl:for-each select="rss/channel">
			<channel>
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="link"/>
			</xsl:attribute>
			<title><xsl:value-of select="title"/></title>
			<link><xsl:value-of select="link"/></link>
			<description><xsl:value-of select="description"/></description>
			<items>
				<rdf:Seq>
					<xsl:for-each select="item">
						<xsl:element name="rdf:li">
							<xsl:attribute name="rdf:resource">
								<xsl:value-of select="link"/>
							</xsl:attribute>
						</xsl:element>
					</xsl:for-each>
				</rdf:Seq>
			</items>
			</channel>
		</xsl:for-each>
		<!-- Parse actual items -->
		<xsl:for-each select="rss/channel/item">
			<item>
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="link"/>
				</xsl:attribute>
				<title><xsl:value-of select="title"/></title>
				<link><xsl:value-of select="link"/></link>
				<description rdf:parseType='Literal'><xsl:value-of select="description"/></description>
			</item>
		</xsl:for-each>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>